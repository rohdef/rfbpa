package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.withError
import arrow.core.right
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

class WeekPlanServiceImplementation(
    private val salarySystem: SalarySystemRepository,
    private val shiftRepository: ShiftRepository,
    private val weekSynchronizationRepository: WeekSynchronizationRepository,
) : WeekPlanService {
    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval
    ): Either<NonEmptyList<SynchronizationError>, Unit> =
        either {
            ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
                nonEmptyListOf(
                    SynchronizationError.InsufficientPermissions(
                        RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                        principal.roles,
                    )
                )
            }

            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(principal.subject, yearWeekInterval)
            val weeksToSynchronize = synchronizationStates
                .filterValues { it == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE }
                .keys
            weeksToSynchronize.mapOrAccumulate { synchronize(principal, it).bind() }
                .bind()
        }

    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeek: YearWeek
    ): Either<SynchronizationError, Unit> = either {
        ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
            SynchronizationError.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                principal.roles,
            )
        }

        val synchronizationState = weekSynchronizationRepository.synchronizationState(principal.subject, yearWeek)
        if (synchronizationState == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE) {
            val salaryWeekPlan = salarySystem.shifts(principal.subject, yearWeek)
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()

            salaryWeekPlan.allShifts.mapOrAccumulate { shiftRepository.createOrUpdate(principal.subject, it).bind() }
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()

            weekSynchronizationRepository.markSynchronized(principal.subject, yearWeek)
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()
        }
    }

    override suspend fun createShift(
        principal: RfbpaPrincipal,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) = either {
        ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
            WeekPlanServiceError.InsufficientPermissions(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN, principal.roles)
        }

        // TODO: 19/08/2024 rohdef -  #41 improve domain errors (i.e., create them)
        when (weekSynchronizationRepository.synchronizationState(principal.subject, start.yearWeek)) {
            WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED ->
                weekSynchronizationRepository.markPossiblyOutOfDate(principal.subject, start.yearWeek)
                    .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }
                    .bind()

            WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE -> {}
            WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE -> {}
        }

        val shift = salarySystem.createShift(principal.subject, start, end)
            .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }
            .bind()

        shiftRepository.createOrUpdate(principal.subject, shift)
            .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }
            .bind()

        shift
    }

    override suspend fun shifts(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval
    ): Either<WeekPlanServiceError, List<WeekPlan>> = either {
        ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
            WeekPlanServiceError.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                principal.roles,
            )
        }

        withError({ it.first().toServiceError() }) {
            synchronize(principal, yearWeekInterval).bind()
        }

        withError({ it.first().toServiceError() }) {
            shiftRepository.byYearWeekInterval(principal.subject, yearWeekInterval).bind()
        }
    }

    override suspend fun changeHelperBooking(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
        helperBooking: HelperBooking
    ): Either<WeekPlanServiceError, Unit> {
        // TODO: 27/10/2024 rohdef - ... dealing with synchronization
        // TODO: 27/10/2024 rohdef - deal with principal
        // TODO: 27/10/2024 rohdef - deal with errors

        when (helperBooking) {
            is HelperBooking.Booked -> salarySystem.bookShift(principal.subject, shiftId, helperBooking.helper)
            HelperBooking.NoBooking -> salarySystem.unbookShift(principal.subject, shiftId)
        }

        shiftRepository.changeBooking(principal.subject, shiftId, helperBooking)

        return Unit.right()
    }

    override suspend fun reportIllness(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
    ): Either<WeekPlanServiceError, Shift> = either {
        val currentShift = shiftRepository.byId(principal.subject, shiftId)
            .mapLeft { it.toServiceError() }
            .bind()

        ensure(currentShift.helperBooking is HelperBooking.Booked) {
            WeekPlanServiceError.ShiftMustBeBooked(shiftId)
        }

        val illnessRegistrations = currentShift.registrations.filterIsInstance<Registration.Illness>()
        if (illnessRegistrations.isEmpty() ) {
            weekSynchronizationRepository.markForSynchronization(principal.subject, currentShift.start.yearWeek)

            val replacementShift = createReplacementShift(principal.subject, currentShift).bind()
            reportAndRegisterIllness(principal.subject, currentShift, replacementShift.shiftId).bind()

            weekSynchronizationRepository.markPossiblyOutOfDate(principal.subject, currentShift.start.yearWeek)

            replacementShift
        } else {
            shiftRepository.byId(principal.subject, illnessRegistrations.first().replacementShiftId)
                .mapLeft { it.toServiceError() }
                .bind()
        }
    }

    suspend private fun createReplacementShift(
        subject: RfbpaPrincipal.Subject,
        shift: Shift
    ): Either<WeekPlanServiceError, Shift> = either {
        val replacementShift = salarySystem.createShift(subject, shift.start, shift.end)
            .mapLeft { it.toServiceError() }
            .bind()
        shiftRepository.createOrUpdate(subject, replacementShift)
            .mapLeft { it.toServiceError() }
            .bind()
        replacementShift
    }

    suspend private fun reportAndRegisterIllness(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
        replacementShiftId: ShiftId,
    ): Either<WeekPlanServiceError, Unit> = either {
        val illShift =
            shift.copy(registrations = shift.registrations + Registration.Illness(replacementShiftId))
        salarySystem.reportIllness(subject, shift.shiftId, replacementShiftId)
            .mapLeft { it.toServiceError() }
            .bind()
        shiftRepository.createOrUpdate(subject, illShift)
            .mapLeft { it.toServiceError() }
            .bind()
    }

    private fun ShiftsError.toServiceError(): WeekPlanServiceError {
        return when (this) {
            ShiftsError.NotAuthorized -> WeekPlanServiceError.CannotCommunicateWithShiftsRepository
            is ShiftsError.ShiftNotFound -> WeekPlanServiceError.ShiftMissingInShiftSystem(shiftId)
        }
    }

    private fun SynchronizationError.toServiceError(): WeekPlanServiceError {
        return when (this) {
            is SynchronizationError.CouldNotSynchronizeWeek -> WeekPlanServiceError.AccessDeniedToSalarySystem
            is SynchronizationError.InsufficientPermissions -> WeekPlanServiceError.InsufficientPermissions(
                expectedRole, actualRoles,
            )
        }
    }

    private fun SalarySystemRepository.RegisterIllnessError.toServiceError(): WeekPlanServiceError {
        return when (this) {
            is SalarySystemRepository.RegisterIllnessError.ShiftNotFound -> WeekPlanServiceError.ShiftMissingInSalarySystem(
                this.shiftId
            )
        }
    }
}
