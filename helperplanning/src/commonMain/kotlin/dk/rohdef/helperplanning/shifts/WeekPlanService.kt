package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.NonEmptyList
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

interface WeekPlanService {
    suspend fun synchronize(yearWeekInterval: YearWeekInterval): Either<NonEmptyList<SynchronizationError>, Unit>
    suspend fun synchronize(yearWeek: YearWeek): Either<SynchronizationError, Unit>
    suspend fun createShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime) : Either<Unit, Shift>
    suspend fun shifts(yearWeekInterval: YearWeekInterval): Either<WeekPlanServiceError, List<WeekPlan>>
}
