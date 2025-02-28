package dk.rohdef.arrowktor

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dk.rohdef.rfbpa.web.ErrorDto
import dk.rohdef.rfbpa.web.NoData
import dk.rohdef.rfbpa.web.UnknownError
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
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

interface RaisedRoute : Route, Raise<ApiError> {
    val routingContext: RoutingContext

    fun principal(): Either<ApiError, dk.rohdef.rfbpa.web.modules.RfbpaPrincipal> = either {
        ensureNotNull(routingContext.call.principal<dk.rohdef.rfbpa.web.modules.RfbpaPrincipal>()) {
            ApiError.forbidden(
                ErrorDto(
                    UnknownError,
                    NoData,
                    "Access denied - you are not logged in",
                )
            )
        }
    }
}


        class RaisedRouteImpl(
    override val routingContext: RoutingContext,
    val route: Route,
    val raise: Raise<ApiError>,
) : RaisedRoute, Route by route, Raise<ApiError> by raise {
    companion object {
        suspend fun <Resource : Any> apply(
            routeContext: RoutingContext,
            route: Route,
            body: suspend RaisedRoute.(Resource) -> HttpResponse<*>,
            resource: Resource,
        ) = either {
            RaisedRouteImpl(
                routeContext,
                route,
                this,
            ).body(resource)
        }
    }
}

inline fun <reified Resource : Any> Route.get(
    noinline body: suspend RaisedRoute.(Resource) -> HttpResponse<*>,
) = resourceGet<Resource> { resource -> handleRequest(this@get, resource, body) }

inline fun <reified Resource : Any> Route.put(
    noinline body: suspend RaisedRoute.(Resource) -> HttpResponse<*>,
) = resourcePut<Resource> { resource -> handleRequest(this@put, resource, body) }

inline suspend fun <reified Resource : Any> RoutingContext.handleRequest(
    route: Route,
    resource: Resource,
    noinline body: suspend RaisedRoute.(Resource) -> HttpResponse<*>,
) {
    val log = KotlinLogging.logger {}

    val result = RaisedRouteImpl.apply(this, route, body, resource)

    when (result) {
        is Either.Left -> {
            log.error { "Error: ${result.value}" }
            call.respond(result.value.status, result.value.error)
        }
        is Either.Right -> call.respond(result.value.status, result.value.message)
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
