package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.RfbpaPrincipal

sealed interface WeekPlanServiceError {
    object AccessDeniedToSalarySystem : WeekPlanServiceError
    object CannotCommunicateWithShiftsRepository : WeekPlanServiceError

    data class InsufficientPermissions(
        val principal: RfbpaPrincipal,
        val expectedRole: RfbpaPrincipal.RfbpaRoles,
    ) : WeekPlanServiceError

    data class ShiftMissingInSalarySystem(
        val shiftId: ShiftId,
    ) : WeekPlanServiceError

    data class ShiftMissingInShiftSystem(
        val shiftId: ShiftId,
    ) : WeekPlanServiceError

    data class ShiftMustBeBooked(
        val shiftId: ShiftId
    ) : WeekPlanServiceError
}
