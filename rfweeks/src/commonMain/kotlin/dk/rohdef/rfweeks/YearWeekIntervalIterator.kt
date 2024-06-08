package dk.rohdef.rfweeks

class YearWeekIntervalIterator(
    first: YearWeek,
    private val endInclusive: YearWeek,
): Iterator<YearWeek> {
    private var next = first
    private var hasNext: Boolean = false

    override fun hasNext(): Boolean = next <= endInclusive

    override fun next(): YearWeek {
        val current = next
        next = next.nextWeek()
        return current
    }
}
