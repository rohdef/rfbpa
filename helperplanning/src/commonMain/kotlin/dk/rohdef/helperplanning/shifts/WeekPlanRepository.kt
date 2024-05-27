package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekRange
import kotlinx.datetime.Instant

interface WeekPlanRepository {
    suspend fun bookShift(
        helper: HelperBooking,
        type: ShiftType,
        start: Instant,
        end: Instant,
    ): Either<Unit, BookingId>

    suspend fun shifts(yearWeeks: YearWeekRange): Either<ShiftsError, WeekPlans> = either {
        val weeks = yearWeeks.map { shifts(it).bind() }
        WeekPlans(weeks)
    }

    suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun createShift(start: Instant, end: Instant, type: ShiftType): Either<Unit, BookingId>
}
