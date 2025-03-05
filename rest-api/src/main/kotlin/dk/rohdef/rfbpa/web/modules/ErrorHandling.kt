package dk.rohdef.rfbpa.web.modules

import dk.rohdef.arrowktor.HttpResponse
import dk.rohdef.rfbpa.web.errors.ErrorData
import dk.rohdef.rfbpa.web.errors.ErrorDto
import dk.rohdef.rfbpa.web.errors.Parsing
import dk.rohdef.rfbpa.web.errors.System
import dk.rohdef.rfweeks.YearWeekIntervalParseError
import dk.rohdef.rfweeks.YearWeekIntervalParseException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.errorHandling() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val error = cause.toError(call)
            call.respond(
                error.status,
                error.message,
            )
        }
    }
}

private fun Throwable.toError(call: ApplicationCall): HttpResponse<ErrorDto> {
    val log = KotlinLogging.logger {}

    return when (this) {
        is BadRequestException -> this.cause!!.toError(call)

        is YearWeekIntervalParseException ->
            HttpResponse.badRequest(
                ErrorDto(
                    Parsing.InvalidYearWeekInterval,
                    ErrorData.MultipleErrors(errors.first().toError()),
                    "Could not parse Year week interval",
                ),
            )

        is IllegalArgumentException -> {
            val path = call.request.path()

            this.toError(path)
        }

        else -> {
            log.error(cause) { "Unknown error occurred" }

            HttpResponse.internalServerError(
                ErrorDto(
                    System.Unknown,
                    ErrorData.NoData,
                    "Non-descript problem, bailing!",
                ),
            )
        }
    }
}

fun IllegalArgumentException.toError(input: String): HttpResponse<ErrorDto> {
    val log = KotlinLogging.logger {}

    val message = this.message
    return when {
        message == null -> {
            log.error(this) { "Something went wrong while parsing the input with an unidentified error" }

            HttpResponse.badRequest(
                ErrorDto(
                    Parsing.Unknown,
                    ErrorData.NoData,
                    "Something went wrong while parsing the input with an unidentified error",
                ),
            )
        }

        message.contains("uuid") -> HttpResponse.badRequest(
            ErrorDto(
                Parsing.InvalidUUID,
                ErrorData.FormatError(
                    input,
                    "UUID as hex string with dashes"
                ),
                "Could not parse Year week interval",
            ),
        )

        else -> {
            log.error(this) { "Something went wrong while parsing the input with an unidentified error" }

            HttpResponse.badRequest(
                ErrorDto(
                    Parsing.Unknown,
                    ErrorData.NoData,
                    "Something went wrong while parsing the input with an unidentified error",
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