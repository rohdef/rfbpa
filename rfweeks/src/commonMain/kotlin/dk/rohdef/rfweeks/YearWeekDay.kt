package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.raise.either
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

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
    fun atTime(hour: Int, minute: Int) =
        YearWeekDayAtTime(this, LocalTime(hour, minute))

    fun nextDay(): YearWeekDay {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> YearWeekDay(yearWeek, DayOfWeek.TUESDAY)
            DayOfWeek.TUESDAY -> YearWeekDay(yearWeek, DayOfWeek.WEDNESDAY)
            DayOfWeek.WEDNESDAY -> YearWeekDay(yearWeek, DayOfWeek.THURSDAY)
            DayOfWeek.THURSDAY -> YearWeekDay(yearWeek, DayOfWeek.FRIDAY)
            DayOfWeek.FRIDAY -> YearWeekDay(yearWeek, DayOfWeek.SATURDAY)
            DayOfWeek.SATURDAY -> YearWeekDay(yearWeek, DayOfWeek.SUNDAY)
            DayOfWeek.SUNDAY -> YearWeekDay(yearWeek.nextWeek(), DayOfWeek.MONDAY)
        }
    }

    private fun DayOfWeek.daysFromMonday() =
        when (this) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }

    companion object {
        fun parseUnsafe(text: String): YearWeekDay {
            val parsed = parse(text)
            return when (parsed) {
                is Either.Right -> parsed.value
                is Either.Left -> TODO()
            }
        }

        fun parse(text: String): Either<Unit, YearWeekDay> = either {
            // TODO: 15/07/2024 rohdef - primitive parsing, assumes dashes exclusively
            // TODO: 15/07/2024 rohdef - primitive parsing, assumes no errors!

            val yearWeek = text.substring(0, 8).let { YearWeek.parse(it) }.mapLeft { }.bind()
            val dayOfWeek = text.substring(9)
                .toInt()
                .let { DayOfWeek(it) }

            YearWeekDay(yearWeek, dayOfWeek)
        }

        fun from(date: LocalDate): YearWeekDay {
            val firstWeekInYear = YearWeekDay(date.year, 1, date.dayOfWeek)
            val firstWeekdayInYear = firstWeekInYear.date
            val daysFromFirstWeekday = firstWeekdayInYear.daysUntil(date)
            val week = daysFromFirstWeekday/7 + 1
            return YearWeekDay(date.year, week, date.dayOfWeek)
        }
    }
}
