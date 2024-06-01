package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.firstOrNone

enum class YearWeekFormat(
    val format: String,
    val regex: Regex,
    val matches: (String) -> Boolean,
) {
    ISO_8601_SHORT(
        "YYYYWww",
        "^[0-9]{4}W[0-9]{2}$".toRegex(),
        { it.matches(ISO_8601_SHORT.regex) },
    ),

    ISO_8601_LONG(
        "YYYY-Www",
        "^[0-9]{4}-W[0-9]{2}$".toRegex(),
        { it.matches(ISO_8601_LONG.regex) },
    );

    companion object {
        fun formatMatch(text: String): Either<FormatMatchError, YearWeekFormat> {
            return YearWeekFormat.entries
                .firstOrNone { it.matches(text) }
                .toEither { FormatMatchError.NoFormatMatches(text) }
        }
    }

    sealed interface FormatMatchError {
        data class NoFormatMatches(val text: String) : FormatMatchError
    }
}
