package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.helperplanning.salary_shifts.SalaryWeekPlan
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

interface SalarySystemRepository {
    suspend fun bookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<BookingError, Unit>

    suspend fun reportIllness(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        replacementShiftId: ShiftId,
    ) : Either<RegisterIllnessError, Unit>

    suspend fun unbookShift(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<BookingError, Unit>

    suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeeks: YearWeekInterval
    ): Either<ShiftsError, List<SalaryWeekPlan>> = either {
        yearWeeks.map { shifts(subject, it).bind() }
    }

    suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, SalaryWeekPlan>

    suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime
    ): Either<ShiftsError, SalaryShift>

    sealed interface BookingError {
        data class ShiftNotFound(val shiftId: ShiftId) : BookingError
        data class HelperNotFound(val helperId: HelperId) : BookingError
    }

    sealed interface RegisterIllnessError {
        data class ShiftNotFound(val shiftId: ShiftId) : RegisterIllnessError
    }
}
