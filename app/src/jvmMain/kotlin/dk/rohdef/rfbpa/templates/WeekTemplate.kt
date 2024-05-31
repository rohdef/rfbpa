package dk.rohdef.rfbpa.templates

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
data class WeekTemplate(
    val name: String,
    val start: String,
    val repeat: String,
    val shifts: Map<String, List<ShiftTemplate>>
)
