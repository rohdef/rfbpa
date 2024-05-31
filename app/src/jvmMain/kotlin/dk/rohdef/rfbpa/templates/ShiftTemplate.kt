package dk.rohdef.rfbpa.templates

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class ShiftTemplate(
    val helper: String? = null,
    val type: String,
    val start: LocalTime,
    val end: LocalTime,
)
