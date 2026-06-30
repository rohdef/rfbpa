package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.rfweeks.YearWeekDayAtTime

interface ShiftsService {
    suspend fun shiftById(principal: RfbpaPrincipal, shiftId: ShiftId): Either<WeekPlanServiceError, Shift>

    suspend fun createShift(
        principal: RfbpaPrincipal,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
        booking: HelperBooking,
    ): Either<WeekPlanServiceError, Shift>

    suspend fun reportIllness(principal: RfbpaPrincipal, shiftId: ShiftId): Either<WeekPlanServiceError, Shift>
    suspend fun bookShift(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
        helperId: HelperId
    ): Either<WeekPlanServiceError, Unit>
}
