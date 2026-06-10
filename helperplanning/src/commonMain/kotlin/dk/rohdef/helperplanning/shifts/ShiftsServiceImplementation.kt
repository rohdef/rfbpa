@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ShiftsServiceImplementation(
    private val salarySystem: SalarySystemRepository,
    private val shiftRepository: ShiftRepository,
    private val helpersRepository: HelpersRepository,
    private val weekSynchronizationRepository: WeekSynchronizationRepository,
    private val time: RfbpaTime<*>,
) : ShiftsService {
    override suspend fun shiftById(
        principal: RfbpaPrincipal,
        shiftId: ShiftId
    ): Either<WeekPlanServiceError, Shift> = either {
        ensureRole(
            principal,
            RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
            WeekPlanServiceError::InsufficientPermissions,
        )

        val shift = shiftRepository.byId(principal.subject, shiftId)
            .mapLeft { WeekPlanServiceError.ShiftMissingInShiftSystem(shiftId) }.bind()

        shift
    }

    override suspend fun createShift(
        principal: RfbpaPrincipal,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
        booking: HelperBooking
    ): Either<WeekPlanServiceError, Shift> = either {
        val salarayShift = salarySystem.createShift(principal.subject, start, end)
            .mapLeft { TODO("Could not book $start -- $end to $booking") }
            .bind()
        if (booking is HelperBooking.Booked) {
            salarySystem.bookShift(principal.subject, salarayShift.shiftId, booking.helper)
                .mapLeft { TODO("Could not book helper (${booking.helper}) for shift ${salarayShift.shiftId}") }
                .bind()
        }
        val shift = Shift.create(
            booking,
            salarayShift.shiftId,
            start,
            end,
            listOf(),
            listOf(),
        ).mapLeft { TODO() }.bind()

        shiftRepository.createOrUpdate(principal.subject, shift)
            .mapLeft { TODO() }
            .bind()
    }

    override suspend fun reportIllness(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
    ): Either<WeekPlanServiceError, Shift> = either {
        val currentShift = shiftRepository.byId(principal.subject, shiftId)
            .mapLeft { it.toServiceError() }.bind()

        ensure(currentShift.helperBooking is HelperBooking.Booked) {
            WeekPlanServiceError.ShiftMustBeBooked(shiftId)
        }

        val illnessRegistrations = currentShift.registrations.filter { it == Registration.Illness }
        if (illnessRegistrations.isEmpty()) {
            weekSynchronizationRepository.markPossiblyOutOfDate(principal.subject, currentShift.start.yearWeek)

            val replacementShift = createReplacementShift(principal.subject, currentShift).bind()
            reportAndRegisterIllness(principal.subject, currentShift, replacementShift.shiftId).bind()

            shiftRepository.byId(principal.subject, replacementShift.shiftId)
                .mapLeft { it.toServiceError() }
                .bind()
        } else {
            // TODO logic flaw - what happens if there's more than one
            // TODO logic flaw - can we accept 0 in this scenario
            currentShift.references
                .filterIsInstance<Reference.From>()
                .filter { it.linkType == Reference.LinkType.ILLNESS }
                .map {
                    shiftRepository.byId(principal.subject, it.id)
                        .mapLeft { it.toServiceError() }
                        .bind()
                }
                .first()
        }
    }

    suspend private fun createReplacementShift(
        subject: RfbpaPrincipal.Subject,
        shift: Shift
    ): Either<WeekPlanServiceError, Shift> = either {
        val replacementShift = salarySystem.createShift(subject, shift.start, shift.end)
            .mapLeft { it.toServiceError() }.bind()
            .toShift(subject)
            .mapLeft { TODO() }.bind()

        shiftRepository.createOrUpdate(subject, replacementShift)
            .mapLeft { it.toServiceError() }.bind()
    }

    private suspend fun reportAndRegisterIllness(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
        replacementShiftId: ShiftId,
    ): Either<WeekPlanServiceError, Unit> = either {
        val illShift = Shift.create(
            shift.helperBooking,
            shift.shiftId,
            shift.start,
            shift.end,
            shift.registrations + Registration.Illness,
            shift.references
        ).mapLeft { TODO() }.bind()
        salarySystem.reportIllness(subject, shift, replacementShiftId)
            .mapLeft { it.toServiceError() }
            .bind()
        shiftRepository.createOrUpdate(subject, illShift)
            .mapLeft { it.toServiceError() }
            .bind()
        shiftRepository.linkShifts(subject, shift.shiftId, replacementShiftId, Reference.LinkType.ILLNESS)
            .mapLeft { it.toServiceError() }
            .bind()
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

    private fun SalarySystemRepository.RegisterIllnessError.toServiceError(): WeekPlanServiceError {
        return when (this) {
            is SalarySystemRepository.RegisterIllnessError.ShiftNotFound -> WeekPlanServiceError.ShiftMissingInSalarySystem(
                this.shiftId
            )
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
}