package dk.rohdef.helperplanning.shifts

data class YearWeekRange(
    override val start: YearWeek,
    override val endInclusive: YearWeek,
) : ClosedRange<YearWeek>, Iterable<YearWeek> {
    override fun iterator() =
        YearWeekRangeIterator(
            start,
            endInclusive,
        )
}