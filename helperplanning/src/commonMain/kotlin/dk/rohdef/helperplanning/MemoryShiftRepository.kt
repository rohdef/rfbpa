package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import kotlinx.datetime.DayOfWeek

class MemoryShiftRepository : ShiftRepository {
    fun reset() {
        _shifts.clear()
    }

    private val _shifts = mutableMapOf<ShiftId, Shift>()

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.toMap()

    override suspend fun byYearWeek(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, WeekPlan> {
        val shiftsForWeek = _shifts.values.filter { it.start.yearWeek == yearWeek }
        val weekPlan = WeekPlan(
            yearWeek,
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

    override suspend fun createOrUpdate(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
    ): Either<ShiftsError, Shift> {
        _shifts.put(shift.shiftId, shift)
        return shift.right()
    }
}
