package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.RfbpaPrincipal

sealed interface WeekPlanServiceError {
    object AccessDeniedToSalarySystem : WeekPlanServiceError
    object CannotCommunicateWithShiftsRepository : WeekPlanServiceError

    data class InsufficientPermissions(
        val expectedRole: RfbpaPrincipal.RfbpaRoles,
        val actualRoles: List<RfbpaPrincipal.RfbpaRoles>,
    )
}
