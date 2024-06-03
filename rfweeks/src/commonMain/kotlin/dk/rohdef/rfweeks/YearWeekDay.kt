package dk.rohdef.rfweeks

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import java.time.DayOfWeek.*

data class YearWeekDay(
    val yearWeek: YearWeek,
    val dayOfWeek: DayOfWeek,
) {
    constructor(year: Int, week: Int, dayOfWeek: DayOfWeek)
            : this(YearWeek(year, week), dayOfWeek)

    val year = yearWeek.year
    val week = yearWeek.week

    fun toLocalDate(): LocalDate {
        // TODO: 02/06/2024 rohdef - icky... only manual test for now :(
        return yearWeek.firstDayOfWeek.plus(dayOfWeek.daysFromMonday(), DateTimeUnit.DAY)
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
}
