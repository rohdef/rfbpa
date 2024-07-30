package dk.rohdef.rfweeks

sealed interface YearWeekIntervalParseError {
    data class NoSeparatorError(
        val text: String,
    ) : YearWeekIntervalParseError

    data class YearWeekComponentParseError(
        val text: String,
        val intervalPart: IntervalPart,
        val yearWeekParseError: YearWeekParseError,
    ) : YearWeekIntervalParseError

    enum class IntervalPart {
        START,
        END,
        // TODO following not yet supported
//        DURATION_FIRST,
//        DURATION_SECOND,
//        UNKNOWN_FIRST,
//        UNKNOWN_LAST,
//        DURATION, // TODO will probably never be supported
    }
}
