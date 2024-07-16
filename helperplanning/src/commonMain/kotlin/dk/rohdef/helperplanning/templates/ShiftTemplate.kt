package dk.rohdef.helperplanning.templates

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class ShiftTemplate(
    val helper: HelperReservation = HelperReservation.NoReservation,
    val start: LocalTime,
    val end: LocalTime,
)
