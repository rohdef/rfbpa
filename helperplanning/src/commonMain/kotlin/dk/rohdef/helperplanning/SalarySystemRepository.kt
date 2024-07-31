package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

interface SalarySystemRepository {
    suspend fun bookShift(
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<BookingError, ShiftId>

    suspend fun shifts(yearWeeks: YearWeekInterval): Either<ShiftsError, List<WeekPlan>> = either {
        yearWeeks.map { shifts(it).bind() }
    }

    suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun createShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Either<Unit, Shift>

    sealed interface BookingError {
        data class ShiftNotFound(val shiftId: ShiftId) : BookingError
    }
}
