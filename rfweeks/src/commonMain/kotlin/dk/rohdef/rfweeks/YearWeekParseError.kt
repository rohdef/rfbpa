package dk.rohdef.rfweeks

sealed interface YearWeekParseError {
    val fullText: String

    data class ContentsLenghtIsWrong(
        override val fullText: String,
        val actualLength: Int,
    ) : YearWeekParseError {
        val minLength: Int = 7
        val maxLength: Int = 8
    }

    data class YearMustBeANumber(
        val actual: String,
        override val fullText: String,
    ) : YearWeekParseError

    data class WeekMustBeANumber(
        val actual: String,
        override val fullText: String,
    ) : YearWeekParseError

    data class WeekMustBePrefixedWithW(
        val actual: String,
        override val fullText: String,
    ) : YearWeekParseError

    data class WeekNumberMustBeTwoDigits(
        val actual: String,
        override val fullText: String,
    ) : YearWeekParseError
}
