package dk.rohdef.helperplanning.shifts

sealed interface Registration {
    object Illness : Registration
}