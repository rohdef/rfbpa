package dk.rohdef.rfbpa.web

import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import arrow.core.raise.withError
import dk.rohdef.arrowktor.ApiError
import dk.rohdef.rfweeks.YearWeekInterval
import dk.rohdef.rfweeks.YearWeekIntervalParseError

// TODO rohdef - use new error context
fun Raise<ApiError>.parseYearWeekInterval(text: String?): YearWeekInterval {
    val yearWeekIntervalParameter = ensureNotNull(text) {
        // TODO rohdef - almost certainly wrong - but what?
        ApiError.badRequest(
            ErrorDto(
                UnknownError,
                "Year week interval must not be null",
                NoData,
            )
        )
    }

    return withError({ it.first().toApiError() }) {
        YearWeekInterval.parse(yearWeekIntervalParameter).bind()
    }
}

// TODO rohdef - use new error context
fun YearWeekIntervalParseError.toApiError(): ApiError {
    return when (this) {
        is YearWeekIntervalParseError.NoSeparatorError ->
            ApiError.badRequest(
                ErrorDto(
                    UnknownError,
                    "Could not find interval separator, please use double hyphen '--'",
                    NoData,
                )
            )

        is YearWeekIntervalParseError.YearWeekComponentParseError ->
            ApiError.badRequest(
                ErrorDto(
                    UnknownError,
                    "Parsing of year weeks failed",
                    NoData,
                )
            )
    }
}
