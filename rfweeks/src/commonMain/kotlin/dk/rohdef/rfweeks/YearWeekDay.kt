package dk.rohdef.rfweeks

import kotlinx.datetime.*
import java.time.DayOfWeek.*

data class YearWeekDay(
    val yearWeek: YearWeek,
    val dayOfWeek: DayOfWeek,
) {
    constructor(year: Int, week: Int, dayOfWeek: DayOfWeek)
            : this(YearWeek(year, week), dayOfWeek)

    val year = yearWeek.year
    val week = yearWeek.week

    val date = yearWeek.firstDayOfWeek.plus(dayOfWeek.daysFromMonday(), DateTimeUnit.DAY)

    fun atTime(time: LocalTime) =
        YearWeekDayAtTime(this, time)

    fun nextDay(): YearWeekDay {
        return when (dayOfWeek) {
            MONDAY -> YearWeekDay(yearWeek, DayOfWeek.TUESDAY)
            TUESDAY -> YearWeekDay(yearWeek, DayOfWeek.WEDNESDAY)
            WEDNESDAY -> YearWeekDay(yearWeek, DayOfWeek.THURSDAY)
            THURSDAY -> YearWeekDay(yearWeek, DayOfWeek.FRIDAY)
            FRIDAY -> YearWeekDay(yearWeek, DayOfWeek.SATURDAY)
            SATURDAY -> YearWeekDay(yearWeek, DayOfWeek.SUNDAY)
            SUNDAY -> YearWeekDay(yearWeek.nextWeek(), DayOfWeek.MONDAY)
        }
    }

    private fun DayOfWeek.daysFromMonday() =
        when (this) {
            MONDAY -> 0
            TUESDAY -> 1
            WEDNESDAY -> 2
            THURSDAY -> 3
            FRIDAY -> 4
            SATURDAY -> 5
            SUNDAY -> 6
        }

    companion object {
        fun from(date: LocalDate): YearWeekDay {
            val firstWeekInYear = YearWeekDay(date.year, 1, date.dayOfWeek)
            val firstWeekdayInYear = firstWeekInYear.date
            val daysFromFirstWeekday = firstWeekdayInYear.daysUntil(date)
            val week = daysFromFirstWeekday/7 + 1
            return YearWeekDay(date.year, week, date.dayOfWeek)
        }
    }
}
