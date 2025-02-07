package dk.rohdef.helperplanning.shifts

sealed interface Registration {
    data class Illness(
        val replacementShiftId: ShiftId,
    ): Registration
}