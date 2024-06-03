package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.shifts.ShiftType
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class ShiftTemplate(
    val helper: HelperReservation = HelperReservation.NoReservation,
    val type: ShiftType,
    val start: LocalTime,
    val end: LocalTime,
)
