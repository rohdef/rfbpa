package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval
import kotlinx.datetime.Instant

interface WeekPlanRepository {
    suspend fun bookShift(
        shiftId: ShiftId,
        helper: HelperBooking.PermanentHelper,
    ): Either<Unit, ShiftId>

    suspend fun shifts(yearWeeks: YearWeekInterval): Either<ShiftsError, WeekPlans> = either {
        val weeks = yearWeeks.map { shifts(it).bind() }
        WeekPlans(weeks)
    }

    suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun createShift(start: Instant, end: Instant, type: ShiftType): Either<Unit, ShiftId>
}
