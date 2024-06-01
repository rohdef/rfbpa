package dk.rohdef.rfweeks

import arrow.core.Either

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
        fun parse(string: String): Either<Unit, YearWeekInterval> {
            TODO()
        }
    }
}
