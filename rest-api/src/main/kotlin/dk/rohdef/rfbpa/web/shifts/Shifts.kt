import arrow.core.raise.either
import arrow.core.raise.withError
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfbpa.web.ApiError
import dk.rohdef.rfbpa.web.modules.rfbpaPrincipal
import dk.rohdef.rfbpa.web.parseYearWeekInterval
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
            val yearWeekInterval = parseYearWeekInterval(call.parameters["yearWeekInterval"])

            val weekPlans = withError({ it.toApiError() }) {
                weekPlanService.shifts(principal, yearWeekInterval).bind()
            }

            weekPlans.map { WeekPlanOut.from(it) }
        }
    }
}

fun WeekPlanServiceError.toApiError(): ApiError {
    return when (this) {
        WeekPlanServiceError.AccessDeniedToSalarySystem ->
            ApiError.forbidden("Access to salary system denied, please check configuration of credentials")

        WeekPlanServiceError.CannotCommunicateWithShiftsRepository ->
            ApiError.internalServerError("Shifts repository unreachable right now, try again later")

        is WeekPlanServiceError.InsufficientPermissions -> TODO()
    }
}
