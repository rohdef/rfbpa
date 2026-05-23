package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.salary_shifts.SalaryBooking
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.helperplanning.salary_shifts.SalaryWeekPlan
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime

class MemorySalarySystemRepository : SalarySystemRepository {
    fun reset() {
        _shifts.clear()
    }

    // TODO: 21/09/2024 rohdef - find isolation level - internal doesn't work
    val _shifts =
        mutableMapOf<RfbpaPrincipal.Subject, Map<ShiftId, SalaryShift>>().withDefault { emptyMap() }

    val shifts: Map<ShiftId, SalaryShift>
        get() = _shifts.map { it.value }
            .fold(emptyMap()) { accumulator, value -> accumulator + value }

    override suspend fun bookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<SalarySystemRepository.BookingError, Unit> = either {
        val helperBooking = SalaryBooking.Helper(helperId)

        val shift = ensureNotNull(_shifts.getValue(subject)[shiftId]) {
            SalarySystemRepository.BookingError.ShiftNotFound(shiftId)
        }.copy(helperBooking = helperBooking)

        _shifts.letValue(subject) { it + (shiftId to shift) }
    }

    override suspend fun reportIllness(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
        replacementShiftId: ShiftId
    ): Either<SalarySystemRepository.RegisterIllnessError, Unit> = either {
        val shiftId = shift.shiftId
        val shift = ensureNotNull(_shifts.getValue(subject)[shiftId]) {
            SalarySystemRepository.RegisterIllnessError.ShiftNotFound(shiftId)
        }
        // TODO remove local date time - will probably fix itself when typed properly???
        val illness = Registration.Illness(LocalDateTime(1885, 1, 1, 0, 0))

        val illShift = shift.copy(registrations = shift.registrations + illness)

        _shifts.letValue(subject) { it + (shiftId to illShift) }
    }

    override suspend fun unbookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId
    ): Either<SalarySystemRepository.BookingError, Unit> = either {
        val helperBooking = SalaryBooking.NoBooking

        val shift = ensureNotNull(_shifts.getValue(subject)[shiftId]) {
            SalarySystemRepository.BookingError.ShiftNotFound(shiftId)
        }.copy(helperBooking = helperBooking)

        _shifts.letValue(subject) { it + (shiftId to shift) }
    }

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, SalaryWeekPlan> = either {
        val shiftsForWeek = _shifts.getValue(subject).values.filter { it.start.yearWeek == yearWeek }
        SalaryWeekPlan(
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
    ): Either<ShiftsError, SalaryShift> = either {
        _shifts[subject] = _shifts.getValue(subject)
        SalaryShift(SalaryBooking.NoBooking, ShiftId.generateId(), start, end)
            .also { shift ->
                _shifts.letValue(subject) { it + (shift.shiftId to shift)}
            }
    }
}
