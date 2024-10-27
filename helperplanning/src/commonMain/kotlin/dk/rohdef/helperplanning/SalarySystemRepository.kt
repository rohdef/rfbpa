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
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<BookingError, Unit>

    suspend fun unbookShift(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<BookingError, Unit>

    suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeeks: YearWeekInterval
    ): Either<ShiftsError, List<WeekPlan>> = either {
        yearWeeks.map { shifts(subject, it).bind() }
    }

    suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, WeekPlan>

    suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime
    ): Either<ShiftsError, Shift>

    sealed interface BookingError {
        data class ShiftNotFound(val shiftId: ShiftId) : BookingError
        data class HelperNotFound(val helperId: HelperId) : BookingError
    }
}
