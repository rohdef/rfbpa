package dk.rohdef.helperplanning.shifts

sealed interface ShiftsError {
    object NotAuthorized : ShiftsError

    data class ShiftNotFound(
        val shift: ShiftId,
    ) : ShiftsError
}
