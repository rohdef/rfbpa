package dk.rohdef.rfbpa.web

import io.ktor.http.*

data class ApiError(
    val status: HttpStatusCode,
    val message: String,
) {
    companion object {
        fun badRequest(message: String) = ApiError(HttpStatusCode.BadRequest, message)
        fun internalServerError(message: String) = ApiError(HttpStatusCode.InternalServerError, message)
        fun forbidden(message: String) = ApiError(HttpStatusCode.Forbidden, message)
    }
}
