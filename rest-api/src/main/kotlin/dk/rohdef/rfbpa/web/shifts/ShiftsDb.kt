package dk.rohdef.rfbpa.web.shifts

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfbpa.web.DatabaseConnection
import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import dk.rohdef.rfweeks.YearWeek
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
import kotlinx.uuid.generateUUID
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.ktor.ext.inject

suspend fun ins() = DatabaseConnection.dbQuery {
    HelpersTable.insert {
        it[id] = UUID.generateUUID().toJavaUUID()
        it[name] = "Fiktivus Maximus"
    }
    HelpersTable.insert {
        it[id] = UUID.generateUUID().toJavaUUID()
        it[name] = "Realis Minimalis"
    }
}

suspend fun fet(): List<Hel> = DatabaseConnection.dbQuery {
    HelpersTable.selectAll()
        .map {
            Hel(
                it[HelpersTable.id].toKotlinUUID(),
                it[HelpersTable.name],
            )
        }
}

private val log = KotlinLogging.logger {}
fun Route.dbShifts() {
    val weekPlansRepository: SalarySystemRepository by inject()
    val wps: WeekPlanService by inject()

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

            val shifts = wps.shifts(yearWeekInterval)
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

    get("/play") {
        val s = weekPlansRepository.shifts(YearWeek(2024, 30))
            .map { it.allShifts.map { Shi.fromShift(it) } }

        when (s) {
            is Either.Right -> call.respond(s.value)
            is Either.Left -> call.respond("Error: ${s.value}")
        }
    }

    get("/seed") {
        ins()

        call.respondText("Seeded")
    }

    get("/db-shifts") {
        val o = fet()
        println(o)

        call.respond(o)
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
) {
    companion object {
        fun fromShift(shift: Shift): Shi {
            val s = shift.start
            val e = shift.end
            return Shi(
                shift.shiftId.id,
                "${s.year}-W${s.week}-${s.dayOfWeek.value}T${s.time.hour}:${s.time.minute}",
                "${e.time.hour}:${e.time.minute}",
            )
        }
    }
}

@Serializable
data class Hel(
    val id: UUID,
    val name: String,
)
