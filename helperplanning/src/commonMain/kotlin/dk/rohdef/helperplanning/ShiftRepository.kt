package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.raise.either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface ShiftRepository {
    suspend fun bookShift(
        shiftId: ShiftId,
        helperId: Helper.ID,
    ): Either<Unit, ShiftId>

    suspend fun shifts(yearWeeks: YearWeekInterval): Either<NonEmptyList<ShiftsError>, WeekPlans> = either {
        val weeks = yearWeeks.mapOrAccumulate { shifts(it).bind() }.bind()
        WeekPlans(weeks)
    }

    suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun createShift(shift: Shift): Either<ShiftsError, Shift>
}
