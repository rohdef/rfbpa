package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.raise.either
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
                .let { DayOfWeek.of(it) }

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
