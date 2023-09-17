package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.traverse
import kotlinx.datetime.Instant

interface WeekPlanRepository {
    suspend fun bookShift(
        helper: HelperBooking,
        type: ShiftType,
        start: Instant,
        end: Instant,
    ): Either<Unit, BookingId>

    suspend fun shifts(yearWeeks: YearWeekRange): Either<ShiftsError, WeekPlans> {
        return yearWeeks.traverse { shifts(it) }
            .map { WeekPlans(it) }
    }

    suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>
}