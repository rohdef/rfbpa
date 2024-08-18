package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.NonEmptyList
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

interface WeekPlanService {
    suspend fun synchronize(principal: RfbpaPrincipal, yearWeekInterval: YearWeekInterval): Either<NonEmptyList<SynchronizationError>, Unit>
    suspend fun synchronize(principal: RfbpaPrincipal, yearWeek: YearWeek): Either<SynchronizationError, Unit>
    suspend fun createShift(principal: RfbpaPrincipal, start: YearWeekDayAtTime, end: YearWeekDayAtTime) : Either<Unit, Shift>
    suspend fun shifts(principal: RfbpaPrincipal, yearWeekInterval: YearWeekInterval): Either<WeekPlanServiceError, List<WeekPlan>>
}
