package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.NonEmptyList

data class YearWeekInterval(
    override val start: YearWeek,
    override val endInclusive: YearWeek,
) : ClosedRange<YearWeek>, Iterable<YearWeek> {
    override fun iterator() =
        YearWeekIntervalIterator(
            start,
            endInclusive,
        )

    companion object {
        fun parse(text: String): Either<NonEmptyList<YearWeekIntervalParseError>, YearWeekInterval> {
            TODO()
        }
    }
}
