package dk.rohdef.rfbpa.web

import arrow.core.Either
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Route.typedGet(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.() -> Either<ApiError, Any>,
) {
    get(path) {
        val res = body()

        when (res) {
            is Either.Left -> call.respond(res.value.status, res.value.message)
            is Either.Right -> call.respond(res.value)
        }
    }
}
