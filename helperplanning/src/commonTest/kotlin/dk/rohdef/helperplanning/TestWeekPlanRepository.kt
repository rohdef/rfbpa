package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import kotlinx.datetime.Instant

class TestWeekPlanRepository : WeekPlanRepository {
    private val _shifts = mutableListOf<Shift>()
    val shifts: List<Shift>
        get() = _shifts.toList()
    val sortedByStartShifts: List<Shift>
        get() = shifts.sortedBy { it.start }

    override suspend fun bookShift(shiftId: ShiftId, helper: HelperBooking.PermanentHelper): Either<Unit, ShiftId> {
        TODO("not implemented")
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        TODO("not implemented")
    }

    override suspend fun createShift(start: Instant, end: Instant, type: ShiftType): Either<Unit, ShiftId> {
        TODO("not implemented")
    }

    data class Shift(
        val start: Instant,
        val end: Instant,
        val type: ShiftType,
    )
}
