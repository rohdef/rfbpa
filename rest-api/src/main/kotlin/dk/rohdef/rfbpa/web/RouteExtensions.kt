package dk.rohdef.rfbpa.web

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import io.ktor.server.resources.get as resourceGet

fun Route.typedPost(
    path: String,
    body: suspend RoutingContext.() -> Either<ApiError, Any>,
) {
    post(path) {
        val res = body()

        when (res) {
            is Either.Left -> call.respond(res.value.status, res.value.message)
            is Either.Right -> call.respond(res.value)
        }
    }
}

inline fun <reified T : Any> Route.get(
    noinline body: suspend RoutingContext.(T) -> Either<ApiError, HttpResponse<*>>
) {
    resourceGet<T> {
        val res = body(it)

        when (res) {
            is Either.Left -> call.respond(res.value.status, res.value.message)
            is Either.Right -> call.respond(res.value.status, res.value.message)
        }
    }
}

fun <U : Any> U.httpOk() = HttpResponse<U>(
    HttpStatusCode.OK,
    this,
)

data class HttpResponse<T : Any>(
    val status: HttpStatusCode,
    val message: T,
)
