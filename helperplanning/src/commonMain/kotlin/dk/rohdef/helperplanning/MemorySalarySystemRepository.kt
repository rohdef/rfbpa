package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import arrow.core.toOption
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek

class MemorySalarySystemRepository : SalarySystemRepository {
    fun reset() {
        _shifts.clear()
    }

    internal val _shifts = mutableMapOf<ShiftId, Shift>()

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.toMap()

    override suspend fun cacheMisses(
        yearWeeks: YearWeekInterval,
        updateStrategy: SalarySystemRepository.UpdateStrategy,
        threshold: DateTimePeriod
    ): Either<Unit, Set<YearWeek>> {
        TODO("not implemented")
    }

    override suspend fun bookShift(
        shiftId: ShiftId,
        helper: Helper.ID
    ): Either<SalarySystemRepository.BookingError, ShiftId> {
        if (!_shifts.containsKey(shiftId)) {
            TODO("Missing shift is currently not handled")
        }
        val shift = _shifts[shiftId].toOption()
            .map { it.copy(helperId = HelperBooking.PermanentHelper(helper)) }
        shift.onSome { _shifts.put(shiftId, it) }

        return shift.map { shiftId }
            .toEither { SalarySystemRepository.BookingError.ShiftNotFound(shiftId) }
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        val shiftsForWeek = _shifts.values.filter { it.start.yearWeek == yearWeek }
        val weekPlan = WeekPlan(
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.MONDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.TUESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.WEDNESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.THURSDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.FRIDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SATURDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SUNDAY },
        )
        return weekPlan.right()
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<Unit, Shift> {
        val shiftId = ShiftId.generateId()
        val shift = Shift(HelperBooking.NoBooking, shiftId, start, end)

        _shifts.put(shiftId, shift)

        return shift.right()
    }
}
