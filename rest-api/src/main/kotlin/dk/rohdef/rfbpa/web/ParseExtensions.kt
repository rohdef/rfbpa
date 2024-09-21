package dk.rohdef.rfbpa.web

import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import arrow.core.raise.withError
import dk.rohdef.rfweeks.YearWeekInterval
import dk.rohdef.rfweeks.YearWeekIntervalParseError
import io.ktor.server.application.*

fun Raise<ApiError>.parseYearWeekInterval(text: String?): YearWeekInterval {
    val yearWeekIntervalParameter = ensureNotNull(text) {
        ApiError.badRequest("Year week interval must not be null")
    }

    return withError({ it.first().toApiError() }) {
        YearWeekInterval.parse(yearWeekIntervalParameter).bind()
    }
}

fun YearWeekIntervalParseError.toApiError() : ApiError {
    return when (this) {
        is YearWeekIntervalParseError.NoSeparatorError ->
            ApiError.badRequest("Could not find interval separator, please use double hyphen '--'")

        is YearWeekIntervalParseError.YearWeekComponentParseError ->
            ApiError.badRequest("Parsing of year weeks failed")
    }
}
