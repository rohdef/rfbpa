package dk.rohdef.rfbpa.web.modules

import dk.rohdef.arrowktor.HttpResponse
import dk.rohdef.rfbpa.web.errors.ErrorData
import dk.rohdef.rfbpa.web.errors.ErrorDto
import dk.rohdef.rfbpa.web.errors.Parsing
import dk.rohdef.rfbpa.web.errors.System
import dk.rohdef.rfweeks.YearWeekIntervalParseError
import dk.rohdef.rfweeks.YearWeekIntervalParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.errorHandling() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val error = cause.toError()
            call.respond(
                error.status,
                error.message,
            )
        }
    }
}

private fun Throwable.toError(): HttpResponse<ErrorDto> {
    val log = KotlinLogging.logger {}

    return when (this) {
        is BadRequestException -> this.cause!!.toError()

        is YearWeekIntervalParseException ->
            HttpResponse(
                HttpStatusCode.BadRequest,
                ErrorDto(
                    Parsing.InvalidYearWeekInterval,
                    ErrorData.MultipleErrors(errors.first().toError()),
                    "Could not parse Year week interval",
                ),
            )

        else -> {
            log.error(cause) { "Unknown error occurred" }

            HttpResponse(
                HttpStatusCode.InternalServerError,
                ErrorDto(
                    System.Unknown,
                    ErrorData.NoData,
                    "Non-descript problem, bailing!",
                ),
            )
        }
    }
}

private fun YearWeekIntervalParseError.toError(): ErrorData {
    val formatDescription = "ISO 8601 [interval](https://en.wikipedia.org/wiki/ISO_8601#Time_intervals) using [week dates](https://en.wikipedia.org/wiki/ISO_8601#Week_dates)."

    return when (this) {
        is YearWeekIntervalParseError.NoSeparatorError -> ErrorData.FormatError(
            text,
            formatDescription,
        )
        is YearWeekIntervalParseError.YearWeekComponentParseError -> ErrorData.FormatError(
            text,
            formatDescription,
        )
    }
}