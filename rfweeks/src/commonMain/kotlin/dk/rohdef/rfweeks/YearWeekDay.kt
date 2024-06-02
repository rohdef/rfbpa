package dk.rohdef.rfweeks

import kotlinx.datetime.DayOfWeek

data class YearWeekDay(
    val year: Int,
    val week: Int,
    val dayOfWeek: DayOfWeek,
)
