@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.shifts

import arrow.core.raise.withError
import dk.rohdef.arrowktor.ApiError
import dk.rohdef.arrowktor.get
import dk.rohdef.arrowktor.httpOk
import dk.rohdef.arrowktor.put
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperService
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsService
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfbpa.web.errors.ErrorData
import dk.rohdef.rfbpa.web.errors.ErrorDto
import dk.rohdef.rfbpa.web.errors.UnknownError
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.resources.*
import io.ktor.server.resources.delete
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val log = KotlinLogging.logger {}
fun Route.shifts() {
    val weekPlanService: WeekPlanService by inject()
    val shiftsService: ShiftsService by inject()
    val helperService: HelperService by inject()

    get<Shifts.InInterval> {
        log.info { "Loading shifts for interval: ${it.yearWeekInterval}" }
        val principal = principal()
            .bind()
            .domainPrincipal

        // TODO - look into this after refactors
        val helpers = helperService.all()
            .associate { it.id to it }
            .withDefault { Helper(it, "Helper not found in system", "Helper not found in system") }

        val weekPlans = withError({ it.toApiError() }) {
            weekPlanService.shifts(principal, it.yearWeekInterval).bind()
        }

        weekPlans.map { WeekPlanOut.from(it, helpers) }.httpOk()
    }

    post<Shifts> {
        log.info { "Pretending to create shift" }

        Uuid.random().httpOk()
    }

    get<Shifts.ById> { shiftById ->
        log.info { "Getting shift by ID: ${shiftById.id}" }

        val principal = principal()
            .bind()
            .domainPrincipal
        val shiftId = ShiftId(shiftById.id)

        // TODO - look into this after refactors
        val helpers = helperService.all()
            .associate { it.id to it }
            .withDefault { Helper(it, "Helper not found in system", "Helper not found in system") }

        val shift = withError({ it.toApiError() }) {
            shiftsService.shiftById(principal, shiftId).bind()
        }

        ShiftOut.from(shift, helpers).httpOk()
    }

    put<Shifts.ById> { shiftById ->
        log.info { "Pretending to update shift ${shiftById.id}" }

        Uuid.random().httpOk()
    }

    delete<Shifts.ById> { shiftById ->
        log.info { "Pretending to delete shift ${shiftById.id}" }
    }

    put<Shifts.ById.Registrations.Illness> { illness ->
        log.info { "Reporting illness for shift ${illness.id}" }

        val principal = principal()
            .bind()
            .domainPrincipal
        val shiftId = ShiftId(illness.id)

        // TODO - look into this after refactors
        val helpers = helperService.all()
            .associate { it.id to it }
            .withDefault { Helper(it, "Helper not found in system", "Helper not found in system") }

        val replacementShift = withError({ it.toApiError() }) {
            shiftsService.reportIllness(principal, shiftId).bind()
        }

        ShiftOut.from(replacementShift, helpers).httpOk()
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
        val id: Uuid,
    ) {
        @Resource("registrations")
        class Registrations(
            val parent: ById,
        ) {
            val id = parent.id
            @Resource("illness")
            class Illness(
                val parent: Registrations,
            ) {
                val id = parent.id
            }
        }
    }
}

fun WeekPlanServiceError.toApiError(): ApiError {
    return when (this) {
        WeekPlanServiceError.AccessDeniedToSalarySystem ->
            ApiError.forbidden(
                ErrorDto(
                    UnknownError,
                    ErrorData.NoData,
                    "Access to salary system denied, please check configuration of credentials.",
                ),
            )

        is WeekPlanServiceError.InsufficientPermissions ->
            ApiError.forbidden(
                ErrorDto(
                    UnknownError,
                    ErrorData.NoData,
                    "Access denied, please ensure that are logged in and that you have the correct permissions.",
                ),
            )

        WeekPlanServiceError.CannotCommunicateWithShiftsRepository ->
            ApiError.internalServerError(
                ErrorDto(
                    UnknownError,
                    ErrorData.NoData,
                    "Shifts repository unreachable right now, try again later.",
                ),
            )

        is WeekPlanServiceError.ShiftMissingInSalarySystem ->
            ApiError.notFound(
                ErrorDto(
                    UnknownError,
                    ErrorData.NoData,
                    "Shift with ID: ${this.shiftId} cound not be found in the external salary system.",
                )
            )

        is WeekPlanServiceError.ShiftMissingInShiftSystem ->
            ApiError.notFound(
                ErrorDto(
                    UnknownError,
                    ErrorData.NoData,
                    "Shift with ID: ${this.shiftId} cound not be found in the RFBPA.",
                )
            )

        is WeekPlanServiceError.ShiftMustBeBooked ->
            ApiError.badRequest(
                ErrorDto(
                    UnknownError,
                    ErrorData.NoData,
                    "Shift with ID: ${this.shiftId} is not booked to a helper. It must be booked.",
                )
            )

        is WeekPlanServiceError.InconsistentIllness -> TODO()
    }
}
