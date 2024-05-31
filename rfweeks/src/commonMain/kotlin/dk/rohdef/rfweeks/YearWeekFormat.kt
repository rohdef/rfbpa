package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.firstOrNone

enum class YearWeekFormat(
    val format: String,
    val regex: Regex,
    val matches: (String) -> Boolean,
) {
    // TODO: 28/05/2024 rohdef - currently not supported
//        ISO_8601_SHORT(
//            "YYYYWww",
//            "^[0-9]{4}W[0-9]{2}$".toRegex(),
//        )

    ISO_8601_LONG(
        "YYYY-Www",
        "^[0-9]{4}-W[0-9]{2}$".toRegex(),
        { it.isBlank() },
    );

    companion object {
        fun formatMatch(text: String): Either<Unit, YearWeekFormat> {
            return YearWeekFormat.entries
                .firstOrNone { it.matches(text) }
                .toEither { }
        }
    }
}
