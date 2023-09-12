package dk.rohdef.helperplanning.shifts

sealed interface ShiftsError {
    object NotAuthorized : ShiftsError
}