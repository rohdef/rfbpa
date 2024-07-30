package dk.rohdef.rfbpa.web.shifts

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfweeks.YearWeekInterval
import dk.rohdef.rfweeks.YearWeekIntervalParseError
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}
fun Route.dbShifts() {
    val weekPlanService: WeekPlanService by inject()

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

    typedGet("/shifts/{yearWeekInterval}") {
        either {
            log.info { "Initial calls of shifts" }

            val yearWeekInterval = call.parameters["yearWeekInterval"]!!
                .let { YearWeekInterval.parse(it) }
                .mapLeft { it.first() }
                .mapLeft {
                    when (it) {
                        is YearWeekIntervalParseError.NoSeparatorError ->
                            ApiError.badRequest("Could not find interval separator, please use double hyphen '--'")
                        is YearWeekIntervalParseError.YearWeekComponentParseError ->
                            ApiError.badRequest("Parsing of year weeks failed")
                    }
                }
                .bind()

            val shifts = weekPlanService.shifts(yearWeekInterval)
                .mapLeft {
                    when (it) {
                        WeekPlanServiceError.AccessDeniedToSalarySystem -> TODO()
                        WeekPlanServiceError.CannotCommunicateWithShiftsRepository -> TODO()
                    }
                }
                .bind()
                .flatMap { it.allShifts }
            shifts.map {
                Shi(
                    it.shiftId.id,
                    it.start.toString(),
                    it.end.toString(),
                )
            }
        }
    }
}

data class ApiError(
    val status: HttpStatusCode,
    val message: String,
) {
    companion object {
        fun badRequest(message: String) = ApiError(HttpStatusCode.BadRequest, message)
    }
}

@Serializable
data class Shi(
    val id: UUID,
    val start: String,
    val end: String,
)
