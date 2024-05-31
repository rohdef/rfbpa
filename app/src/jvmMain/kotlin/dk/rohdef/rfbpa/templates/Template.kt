package dk.rohdef.rfbpa.templates

import kotlinx.serialization.Serializable

@Serializable
data class Template(
    val start: String,
    val end: String,
    // TODO: 27/05/2024 rohdef - validate that the weeks will never overlap
    val weeks: List<WeekTemplate>,
)
