import arrow.core.raise.withError
import dk.rohdef.arrowktor.ApiError
import dk.rohdef.arrowktor.get
import dk.rohdef.arrowktor.httpOk
import dk.rohdef.arrowktor.put
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperService
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfbpa.web.ErrorDto
import dk.rohdef.rfbpa.web.NoData
import dk.rohdef.rfbpa.web.UnknownError
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
        log.info { "Loading shifts for interval: ${it.yearWeekInterval}" }
        val principal = principal()
            .bind()
            .domainPrincipal
        val weekPlans = withError({ it.toApiError() }) {
            weekPlanService.shifts(principal, it.yearWeekInterval!!).bind()
        }
        val helpers = helperService.all()
            .associate { it.id to it }
            .withDefault { Helper.Unknown("Helper not found in system", it) }

        weekPlans.map { WeekPlanOut.from(it, helpers) }.httpOk()
    }

    get<Shifts.ById> { shiftById ->
        log.info { "Getting shift by ID: ${shiftById.id}" }

        raise(WeekPlanServiceError.ShiftMissingInShiftSystem(ShiftId(shiftById.id)).toApiError())

        "Having fun ${shiftById.id}?".httpOk()
    }

    put<Shifts> {
        log.info { "Reporting illness" }

//            weekPlanService.reportIllness(TODO(), ShiftId(it.id))

        "Samuel".httpOk()
    }
}

@Resource("/shifts")
class Shifts {
    @Resource("in-interval/{yearWeekInterval}")
    class InInterval(
        val parent: Shifts = Shifts(),
        val yearWeekInterval: YearWeekInterval,
    )

    @Resource("{id}")
    class ById(
        val parent: Shifts = Shifts(),
        val id: UUID,
    )
}

fun WeekPlanServiceError.toApiError(): ApiError {
    return when (this) {
        WeekPlanServiceError.AccessDeniedToSalarySystem ->
            ApiError.forbidden(
                ErrorDto(
                    UnknownError,
                    "Access to salary system denied, please check configuration of credentials.",
                    NoData,
                ),
            )

        is WeekPlanServiceError.InsufficientPermissions ->
            ApiError.forbidden(
                ErrorDto(
                    UnknownError,
                    "Access denied, please ensure that are logged in and that you have the correct permissions.",
                    NoData,
                ),
            )

        WeekPlanServiceError.CannotCommunicateWithShiftsRepository ->
            ApiError.internalServerError(
                ErrorDto(
                    UnknownError,
                    "Shifts repository unreachable right now, try again later.",
                    NoData,
                ),
            )

        is WeekPlanServiceError.ShiftMissingInSalarySystem ->
            ApiError.notFound(
                ErrorDto(
                    UnknownError,
                    "Shift with ID: ${this.shiftId} cound not be found in the external salary system.",
                    NoData,
                )
            )

        is WeekPlanServiceError.ShiftMissingInShiftSystem ->
            ApiError.notFound(
                ErrorDto(
                    UnknownError,
                    "Shift with ID: ${this.shiftId} cound not be found in the RFBPA.",
                    NoData,
                )
            )

        is WeekPlanServiceError.ShiftMustBeBooked ->
            ApiError.badRequest(
                ErrorDto(
                    UnknownError,
                    "Shift with ID: ${this.shiftId} is not booked to a helper. It must be booked.",
                    NoData,
                )
            )
    }
}
