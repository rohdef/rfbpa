import arrow.core.raise.either
import arrow.core.raise.withError
import dk.rohdef.arrowktor.ApiError
import dk.rohdef.arrowktor.get
import dk.rohdef.arrowktor.put
import dk.rohdef.arrowktor.httpOk
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperService
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfbpa.web.ErrorDto
import dk.rohdef.rfbpa.web.NoData
import dk.rohdef.rfbpa.web.UnknownErrorType
import dk.rohdef.rfbpa.web.modules.rfbpaPrincipal
import dk.rohdef.rfbpa.web.shifts.WeekPlanOut
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.resources.*
import io.ktor.server.routing.*
import kotlinx.uuid.UUID
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}
fun Route.shifts() {
    val weekPlanService: WeekPlanService by inject()
    val helperService: HelperService by inject()

    get<Shifts.InInterval> {
        either {
            log.info { "Loading shifts for interval: ${it.yearWeekInterval}" }
            val principal = call.rfbpaPrincipal().bind()
            val weekPlans = withError({ it.toApiError() }) {
                weekPlanService.shifts(principal, it.yearWeekInterval).bind()
            }
            val helpers = helperService.all()
                .associate { it.id to it }
                .withDefault { Helper.Unknown("Helper not found in system", it) }

            weekPlans.map { WeekPlanOut.from(it, helpers) }.httpOk()
        }
    }

    get<Shift.ById> { shiftById ->
        either {
            log.info { "Getting shift by ID: ${shiftById.id}" }

            val x = ApiError.forbidden(
                ErrorDto(
                    UnknownErrorType,
                    "some description",
                    NoData("Klaphat"),
                )
            )

            raise(x)

            "Having fun ${shiftById.id}?".httpOk()
        }
    }

//    put<Shift.ById> {
//        either {
//            log.info { "Reporting illness" }
//
//            weekPlanService.reportIllness(TODO())
//
//            "hey".httpOk()
//        }
//    }
}

@Resource("/shifts")
class Shifts {
    @Resource("{yearWeekInterval}")
    class InInterval(
        val parent: Shifts = Shifts(),
        val yearWeekInterval: YearWeekInterval,
    )
}

@Resource("/shifts")
class Shift {
    @Resource("{id}")
    class ById(
        val parent: Shift = Shift(),
        val id: UUID,
    )
}

// TODO rohdef - use new error context
fun WeekPlanServiceError.toApiError(): ApiError {
    return when (this) {
        WeekPlanServiceError.AccessDeniedToSalarySystem ->
            ApiError.forbidden(
                ErrorDto(
                    UnknownErrorType,
                    "Access to salary system denied, please check configuration of credentials",
                    NoData("No access"),
                ),
            )

        WeekPlanServiceError.CannotCommunicateWithShiftsRepository ->
            ApiError.internalServerError(
                ErrorDto(
                    UnknownErrorType,
                    "Shifts repository unreachable right now, try again later",
                    NoData(),
                ),
            )

        is WeekPlanServiceError.InsufficientPermissions -> TODO()
    }
}
