package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface ShiftRepository {
    suspend fun byYearWeekInterval(yearWeeks: YearWeekInterval): Either<NonEmptyList<ShiftsError>, List<WeekPlan>> = either {
        yearWeeks.mapOrAccumulate { byYearWeek(it).bind() }.bind()
    }

    suspend fun byYearWeek(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun create(shift: Shift): Either<ShiftsError, Shift>
}
