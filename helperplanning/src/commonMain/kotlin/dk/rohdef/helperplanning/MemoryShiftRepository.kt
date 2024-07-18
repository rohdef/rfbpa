package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime

class MemoryShiftRepository : ShiftRepository {
    fun reset() {
        _shifts.clear()
    }

    private val _shifts = mutableMapOf<ShiftId, Shift>()

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.toMap()

    override suspend fun bookShift(shiftId: ShiftId, helperId: Helper.ID): Either<Unit, ShiftId> {
        TODO("not implemented")
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        TODO("not implemented")
    }

    override suspend fun createShift(
        shift: Shift,
    ): Either<Unit, Shift> {
        TODO("not implemented")
    }
}
