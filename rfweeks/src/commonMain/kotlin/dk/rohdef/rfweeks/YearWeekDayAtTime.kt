package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.raise.either
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime

data class YearWeekDayAtTime(
    val yearWeekDay: YearWeekDay,
    val time: LocalTime,
) {
    val yearWeek = yearWeekDay.yearWeek
    val year = yearWeekDay.year
    val week = yearWeekDay.week
    val dayOfWeek = yearWeekDay.dayOfWeek

    val date = yearWeekDay.date

    val localDateTime = date.atTime(time)

    companion object {
        fun parseUnsafe(text: String): YearWeekDayAtTime {
            val parsed = parse(text)
            return when (parsed) {
                is Either.Right -> parsed.value
                is Either.Left -> TODO()
            }
        }

        fun parse(text: String): Either<Unit, YearWeekDayAtTime> = either {
            // TODO: 15/07/2024 rohdef - primitive parsing, assumes dashes exclusively
            // TODO: 15/07/2024 rohdef - primitive parsing, assumes no errors!

            val yearWeekDay = text.substring(0, 10).let { YearWeekDay.parse(it) }.mapLeft { }.bind()
            val time = text.substring(11)
                .let { LocalTime.parse(it) }

            YearWeekDayAtTime(yearWeekDay, time)
        }

        fun from(dateTime: LocalDateTime): YearWeekDayAtTime {
            return YearWeekDayAtTime(
                YearWeekDay.from(dateTime.date),
                dateTime.time,
            )
        }
    }
}
