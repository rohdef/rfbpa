package dk.rohdef.rfweeks

import arrow.core.Either

data class YearWeekRange(
    override val start: YearWeek,
    override val endInclusive: YearWeek,
) : ClosedRange<YearWeek>, Iterable<YearWeek> {
    override fun iterator() =
        YearWeekRangeIterator(
            start,
            endInclusive,
        )

    companion object {
        fun parse(string: String): Either<Unit, YearWeekRange> {
            TODO()
        }
    }
}
