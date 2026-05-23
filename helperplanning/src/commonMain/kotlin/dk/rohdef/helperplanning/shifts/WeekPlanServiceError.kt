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

    // TODO this is an exception candidate,
    //  no reasonable system would report an illness of an ill helper from AXP
    // it might be worth considering a helper for creating replacement shifts
    // but that should be explicit
    data class InconsistentIllness(
        val shiftId: ShiftId
    ) : WeekPlanServiceError
}
