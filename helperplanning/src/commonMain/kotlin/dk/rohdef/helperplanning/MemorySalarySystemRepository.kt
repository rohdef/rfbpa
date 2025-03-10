package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.DayOfWeek

class MemorySalarySystemRepository : SalarySystemRepository {
    fun reset() {
        _shifts.clear()
    }

    // TODO: 21/09/2024 rohdef - find isolation level - internal doesn't work
    val _shifts =
        mutableMapOf<RfbpaPrincipal.Subject, Map<ShiftId, Shift>>().withDefault { emptyMap() }

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.map { it.value }
            .fold(emptyMap()) { accumulator, value -> accumulator + value }

    override suspend fun bookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<SalarySystemRepository.BookingError, Unit> = either {
        val helperBooking = HelperBooking.Booked(helperId)

        val shift = ensureNotNull(_shifts.getValue(subject)[shiftId]) {
            SalarySystemRepository.BookingError.ShiftNotFound(shiftId)
        }.copy(helperBooking = helperBooking)

        _shifts.letValue(subject) { it + (shiftId to shift) }
    }

    override suspend fun reportIllness(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        replacementShiftId: ShiftId
    ): Either<SalarySystemRepository.RegisterIllnessError, Unit> = either {
        val shift = ensureNotNull(_shifts.getValue(subject)[shiftId]) {
            SalarySystemRepository.RegisterIllnessError.ShiftNotFound(shiftId)
        }
        val illness = Registration.Illness(replacementShiftId)

        val illShift = shift.copy(registrations = shift.registrations + illness)

        _shifts.letValue(subject) { it + (shiftId to illShift) }
    }

    override suspend fun unbookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId
    ): Either<SalarySystemRepository.BookingError, Unit> = either {
        val helperBooking = HelperBooking.NoBooking

        val shift = ensureNotNull(_shifts.getValue(subject)[shiftId]) {
            SalarySystemRepository.BookingError.ShiftNotFound(shiftId)
        }.copy(helperBooking = helperBooking)

        _shifts.letValue(subject) { it + (shiftId to shift) }
    }

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, WeekPlan> = either {
        val shiftsForWeek = _shifts.getValue(subject).values.filter { it.start.yearWeek == yearWeek }
        WeekPlan(
            yearWeek,
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.MONDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.TUESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.WEDNESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.THURSDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.FRIDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SATURDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SUNDAY },
        )
    }

    override suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<ShiftsError, Shift> = either {
        _shifts[subject] = _shifts.getValue(subject)
        Shift(HelperBooking.NoBooking, ShiftId.generateId(), start, end)
            .also { shift ->
                _shifts.letValue(subject) { it + (shift.shiftId to shift)}
            }
    }
}
