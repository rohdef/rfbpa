import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}
fun Route.shifts() {
    val weekPlansRepository: SalarySystemRepository by inject()

    get("/shifts/{yearWeekInterval}") {
        log.info { "Initial calls of shifts" }

        val ywi = call.parameters["yearWeekInterval"]!!

        val res = either {
            val yearWeekInterval = YearWeekInterval.parse(ywi).bind()
            log.info { yearWeekInterval }
            val shifts = weekPlansRepository.shifts(yearWeekInterval).bind()
            shifts
        }

        when(res) {
            is Either.Right -> call.respond("Hello shifts ${res.value}")
            is Either.Left -> TODO()
        }
    }
}
