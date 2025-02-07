package dk.rohdef.arrowktor

import dk.rohdef.rfbpa.web.ErrorDto
import io.ktor.http.*

data class ApiError(
    val status: HttpStatusCode,
    val error: ErrorDto,
) {
    companion object {
        fun badRequest(error: ErrorDto) =
            ApiError(HttpStatusCode.BadRequest, error)

        fun internalServerError(error: ErrorDto) =
            ApiError(HttpStatusCode.InternalServerError, error)

        fun forbidden(error: ErrorDto) =
            ApiError(HttpStatusCode.Forbidden, error)

        fun unauthorized(error: ErrorDto) =
            ApiError(HttpStatusCode.Unauthorized, error)
    }
}