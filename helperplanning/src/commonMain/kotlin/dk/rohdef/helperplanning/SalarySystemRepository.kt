package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import kotlinx.datetime.DateTimePeriod

interface SalarySystemRepository {
    suspend fun bookShift(
        shiftId: ShiftId,
        helperId: Helper.ID,
    ): Either<BookingError, ShiftId>

    suspend fun shifts(yearWeeks: YearWeekInterval): Either<ShiftsError, WeekPlans> = either {
        val weeks = yearWeeks.map { shifts(it).bind() }
        WeekPlans(weeks)
    }

    suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun createShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Either<Unit, Shift>

    sealed interface BookingError {
        data class ShiftNotFound(val shiftId: ShiftId) : BookingError
    }
}
