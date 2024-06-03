package dk.rohdef.helperplanning.templates

import dk.rohdef.rfweeks.YearWeek
import kotlinx.serialization.Serializable

@Serializable
data class Template(
    val start: YearWeek,
    // TODO: 27/05/2024 rohdef - validate that the weeks will never overlap
    val weeks: List<WeekTemplate>,
)
