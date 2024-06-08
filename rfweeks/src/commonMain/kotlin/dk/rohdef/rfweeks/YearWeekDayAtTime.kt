package dk.rohdef.rfweeks

import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime

data class YearWeekDayAtTime(
    val yearWeekDay: YearWeekDay,
    val time: LocalTime,
) {
    val year = yearWeekDay.year
    val week = yearWeekDay.week
    val dayOfWeek = yearWeekDay.dayOfWeek

    val date = yearWeekDay.date

    val localDateTime = date.atTime(time)
}
