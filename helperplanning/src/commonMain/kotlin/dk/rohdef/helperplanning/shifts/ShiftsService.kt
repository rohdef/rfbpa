package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import dk.rohdef.helperplanning.RfbpaPrincipal

interface ShiftsService {
    suspend fun shiftById(principal: RfbpaPrincipal, shiftId: ShiftId): Either<WeekPlanServiceError, Shift>

    suspend fun reportIllness(principal: RfbpaPrincipal, shiftId: ShiftId): Either<WeekPlanServiceError, Shift>
}
