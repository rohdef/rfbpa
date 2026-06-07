@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts

import arrow.core.*
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.withError
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WeekPlanServiceImplementation(
    private val salarySystem: SalarySystemRepository,
    private val shiftRepository: ShiftRepository,
    private val helpersRepository: HelpersRepository,
    private val weekSynchronizationRepository: WeekSynchronizationRepository,
) : WeekPlanService {
    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval
    ): Either<NonEmptyList<SynchronizationError>, Unit> = either {
        ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
            nonEmptyListOf(
                SynchronizationError
                    .InsufficientPermissions(
                        principal,
                        RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
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
        ensureRole(
            principal,
            RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
            SynchronizationError::InsufficientPermissions,
        )

        val synchronizationState = weekSynchronizationRepository.synchronizationState(principal.subject, yearWeek)
        if (synchronizationState == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE) {
            val salaryWeekPlan = salarySystem.shifts(principal.subject, yearWeek)
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()

            salaryWeekPlan.allShifts
                .map {
                    it.toShift(principal.subject)
                        .mapLeft { TODO() }.bind()
                }
                .mapOrAccumulate { shiftRepository.createOrUpdate(principal.subject, it).bind() }
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()


            weekSynchronizationRepository.markSynchronized(principal.subject, yearWeek)
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()
        }
    }

    private suspend fun SalaryShift.toShift(subject: RfbpaPrincipal.Subject): Either<Unit, Shift> = either {
        val existingBooking: suspend () -> Either<Unit, HelperId> = {
            shiftRepository.findBooking(subject, shiftId)
                .fold({
                    when (it) {
                        ShiftsError.NotAuthorized -> Unit.left()
                        is ShiftsError.ShiftNotFound -> helpersRepository.create(
                            dk.rohdef.helperplanning.helpers.Helper(
                                HelperId.generateId(),
                                "Vacancy",
                                Uuid.random().toHexString(),
                            )
                        )
                            .map { it.id }
                            .mapLeft { }
                    }
                })
                { it.right() }
        }

        toShift(existingBooking).bind()
    }

    override suspend fun createShift(
        principal: RfbpaPrincipal,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) = either {
        ensureRole(
            principal,
            RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
            WeekPlanServiceError::InsufficientPermissions,
        )

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
            .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }.bind()
            .toShift(principal.subject)
            .mapLeft { TODO() }.bind()

        shiftRepository.createOrUpdate(principal.subject, shift)
            .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }.bind()

        shift
    }

    override suspend fun shifts(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval
    ): Either<WeekPlanServiceError, List<WeekPlan>> = either {
        ensureRole(
            principal,
            RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
            WeekPlanServiceError::InsufficientPermissions
        )

        withError({ it.first().toServiceError() }) {
            synchronize(principal, yearWeekInterval).bind()
        }

        withError({ it.first().toServiceError() }) {
            shiftRepository.byYearWeekInterval(principal.subject, yearWeekInterval).bind()
        }
    }

    override suspend fun bookHelper(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
        helperId: HelperId
    ): Either<WeekPlanServiceError, Unit> = either {
        // TODO: 27/10/2024 rohdef - ... dealing with synchronization
        // TODO: 27/10/2024 rohdef - deal with principal
        // TODO: 27/10/2024 rohdef - deal with errors

        salarySystem.bookShift(principal.subject, shiftId, helperId)
            .mapLeft { TODO() }.bind()
        shiftRepository.changeBooking(principal.subject, shiftId, HelperBooking.Booked(helperId))
            .mapLeft { TODO() }.bind()
    }

    override suspend fun unbookHelper(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
    ): Either<WeekPlanServiceError, Unit> = either {
        salarySystem.unbookShift(principal.subject, shiftId)
            .mapLeft { TODO() }.bind()
        shiftRepository.unbookShift(principal.subject, shiftId)
            .mapLeft { TODO() }.bind()
    }

    private fun <ErrorType> Raise<ErrorType>.ensureRole(
        principal: RfbpaPrincipal,
        role: RfbpaPrincipal.RfbpaRoles,
        raise: (RfbpaPrincipal, RfbpaPrincipal.RfbpaRoles) -> ErrorType,
    ) {
        ensure(principal.roles.contains(role)) {
            raise(principal, role)
        }
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
                principal,
                expectedRole,
            )
        }
    }
}
