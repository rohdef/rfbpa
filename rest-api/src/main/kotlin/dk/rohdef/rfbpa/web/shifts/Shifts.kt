import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfbpa.web.ApiError
import dk.rohdef.rfbpa.web.modules.rfbpaPrincipal
import dk.rohdef.rfbpa.web.shifts.WeekPlanOut
import dk.rohdef.rfbpa.web.typedGet
import dk.rohdef.rfweeks.YearWeekInterval
import dk.rohdef.rfweeks.YearWeekIntervalParseError
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}
fun Route.shifts() {
    val weekPlanService: WeekPlanService by inject()

    typedGet("/shifts/{yearWeekInterval}") {
        either {
            log.info { "Loading shifts for interval: ${call.parameters["yearWeekInterval"]}" }
            val principal = call.rfbpaPrincipal().bind()

            // // TODO: 15/09/2024 rohdef - make extention method or similar, perhaps something on parameters?
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

            val weekPlans = weekPlanService.shifts(principal, yearWeekInterval)
                .mapLeft {
                    when (it) {
                        WeekPlanServiceError.AccessDeniedToSalarySystem ->
                            ApiError.forbidden("Access to salary system denied, please check configuration of credentials")

                        WeekPlanServiceError.CannotCommunicateWithShiftsRepository ->
                            ApiError.internalServerError("Shifts repository unreachable right now, try again later")
                    }
                }
                .bind()

            weekPlans.map { WeekPlanOut.from(it) }
        }
    }
}
