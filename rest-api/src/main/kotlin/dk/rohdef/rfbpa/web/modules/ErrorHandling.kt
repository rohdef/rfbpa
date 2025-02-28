package dk.rohdef.rfbpa.web.modules

import dk.rohdef.arrowktor.HttpResponse
import dk.rohdef.rfbpa.web.errors.ErrorDto
import dk.rohdef.rfbpa.web.errors.NoData
import dk.rohdef.rfbpa.web.errors.System
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

        is YearWeekIntervalParseException -> HttpResponse(
            HttpStatusCode(418, "I'm a tea pot"),
            ErrorDto(
                System.Unknown,
                NoData,
                "AAAAAAA!",
            ),
        )

        else -> {
            log.error(cause) { "Unknown error occurred" }

            HttpResponse(
                HttpStatusCode.InternalServerError,
                ErrorDto(
                    System.Unknown,
                    NoData,
                    "Non-descript problem, bailing!",
                ),
            )
        }
    }
}