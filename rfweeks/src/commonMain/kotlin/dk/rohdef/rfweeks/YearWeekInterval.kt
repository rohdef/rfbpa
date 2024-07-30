package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate

data class YearWeekInterval(
    override val start: YearWeek,
    override val endInclusive: YearWeek,
) : ClosedRange<YearWeek>, Iterable<YearWeek> {
    override fun iterator() =
        YearWeekIntervalIterator(
            start,
            endInclusive,
        )

    override fun toString(): String {
        return "${start}--${endInclusive}"
    }

    companion object {
        private const val solidusSeparator = "/"
        private const val hyphenSeparator = "--"
        fun parse(text: String): Either<NonEmptyList<YearWeekIntervalParseError>, YearWeekInterval> = either {
            val timespecifications = if (text.contains('/')) {
                text.split(solidusSeparator)
            } else {
                text.split(hyphenSeparator)
            }

            ensure(timespecifications.size != 1) { nonEmptyListOf(YearWeekIntervalParseError.NoSeparatorError(text)) }
            if (timespecifications.size >= 2) {
            }

            val start = timespecifications[0]
            val end = timespecifications[1]
            // TODO: 02/06/2024 rohdef - further ways to be wrong (is this actually a parse error?)
//            if (first.isBlank()) { fail("$durationFormat. First week specification is empty.") }
//            if (last.isBlank()) { fail("$durationFormat. Last week specification is empty.") }

            zipOrAccumulate(
                {
                    YearWeek.parse(start)
                        .mapLeft {
                            YearWeekIntervalParseError.YearWeekComponentParseError(
                                text,
                                YearWeekIntervalParseError.IntervalPart.START,
                                it,
                            )
                        }
                        .bind()
                },
                {
                    YearWeek.parse(end)
                        .mapLeft {
                            YearWeekIntervalParseError.YearWeekComponentParseError(
                                text,
                                YearWeekIntervalParseError.IntervalPart.END,
                                it,
                            )
                        }
                        .bind()
                },
            ) {
              firstWeek, lastWeek -> firstWeek..lastWeek
            }
        }
    }
}
