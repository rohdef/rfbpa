@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.salary_shifts.SalaryBooking
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
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

    override suspend fun reportIllness(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
    ): Either<WeekPlanServiceError, Shift> = either {
        val currentShift = shiftRepository.byId(principal.subject, shiftId)
            .mapLeft { it.toServiceError() }.bind()

        ensure(currentShift.helperBooking is HelperBooking.Booked) {
            WeekPlanServiceError.ShiftMustBeBooked(shiftId)
        }

        val illnessRegistrations = currentShift.registrations.filterIsInstance<Registration.Illness>()
        if (illnessRegistrations.isEmpty()) {
            weekSynchronizationRepository.markPossiblyOutOfDate(principal.subject, currentShift.start.yearWeek)

            val replacementShift = createReplacementShift(principal.subject, currentShift).bind()
            reportAndRegisterIllness(principal.subject, currentShift, replacementShift.shiftId).bind()

            replacementShift
        } else {
            val illnessRegistration = illnessRegistrations.first()
            illnessRegistration.replacementShiftId
                .toEither { WeekPlanServiceError.InconsistentIllness(shiftId) }
                .flatMap {
                    shiftRepository.byId(principal.subject, it)
                        .mapLeft { it.toServiceError() }
                }
                .bind()
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
        replacementShift
    }

    private suspend fun reportAndRegisterIllness(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
        replacementShiftId: ShiftId,
    ): Either<WeekPlanServiceError, Unit> = either {
        val illShift =
            shift.copy(registrations = shift.registrations + Registration.Illness(replacementShiftId))
        salarySystem.reportIllness(subject, shift, replacementShiftId)
            .mapLeft { it.toServiceError() }
            .bind()
        shiftRepository.createOrUpdate(subject, illShift)
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

        Shift(
            helperBooking.toBooking(existingBooking).bind(),
            shiftId,
            start,
            end,
            registrations,
        )
    }

    private suspend fun SalaryBooking.toBooking(
        findOrCreateBooking: suspend () -> Either<Unit, HelperId>
    ): Either<Unit, HelperBooking> = either {
        when (this@toBooking) {
            is SalaryBooking.Helper -> HelperBooking.Booked(helper)
            SalaryBooking.NoBooking -> HelperBooking.NoBooking
            is SalaryBooking.UnknownHelper -> HelperBooking.Booked(helper)
            SalaryBooking.Vacancy -> findOrCreateBooking().bind()
                .let { HelperBooking.Booked(it) }
        }
    }
}