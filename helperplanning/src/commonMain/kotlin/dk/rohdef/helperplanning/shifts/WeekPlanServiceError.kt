package dk.rohdef.helperplanning.shifts

sealed interface WeekPlanServiceError {
    object AccessDeniedToSalarySystem : WeekPlanServiceError
    object CannotCommunicateWithShiftsRepository : WeekPlanServiceError
}
