package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.traverse

interface WeekPlanRepository {
    suspend fun shifts(yearWeeks: YearWeekRange): Either<ShiftsError, WeekPlans> {
        return yearWeeks.traverse { shifts(it) }
            .map { WeekPlans(it) }
    }

    suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>
}