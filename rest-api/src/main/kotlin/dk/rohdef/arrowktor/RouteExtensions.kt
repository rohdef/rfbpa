package dk.rohdef.arrowktor

import arrow.core.Either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.get as resourceGet
import io.ktor.server.resources.put as resourcePut

fun Route.typedPost (
    path: String,
    body: suspend RoutingContext.() -> Either<ApiError, Any>,
) {
    post(path) {
        val res = body()

        when (res) {
            is Either.Left -> call.respond(res.value.status, res.value.error)
            is Either.Right -> call.respond(res.value)
        }
    }
}

inline fun <reified T : Any> Route.get(
    noinline body: suspend RoutingContext.(T) -> Either<ApiError, HttpResponse<*>>
) {
    resourceGet<T> {
        val log = KotlinLogging.logger {}
        val res = body(it)

        when (res) {
            is Either.Left -> {
                log.error { "Error: ${res.value}" }
                call.respond(res.value.status, res.value.error)
            }
            is Either.Right -> call.respond(res.value.status, res.value.message)
        }
    }
}

inline fun <reified T : Any> Route.put(
    noinline body: suspend RoutingContext.(T) -> Either<ApiError, HttpResponse<*>>
) {
    resourcePut<T> {
        val log = KotlinLogging.logger {}
        val res = body(it)

        when (res) {
            is Either.Left -> {
                log.error { "Error: ${res.value}" }
                call.respond(res.value.status, res.value.error)
            }
            is Either.Right -> call.respond(res.value.status, res.value.message)
        }
    }
}

fun <U : Any> U.httpOk() = HttpResponse(
    HttpStatusCode.OK,
    this,
)

data class HttpResponse<T : Any>(
    val status: HttpStatusCode,
    val message: T,
)
