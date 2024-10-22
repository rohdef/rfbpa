import arrow.core.getOrElse
import arrow.core.nonEmptySetOf
import arrow.core.raise.either
import arrow.core.raise.withError
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceError
import dk.rohdef.rfbpa.web.ApiError
import dk.rohdef.rfbpa.web.get
import dk.rohdef.rfbpa.web.httpOk
import dk.rohdef.rfbpa.web.modules.rfbpaPrincipal
import dk.rohdef.rfbpa.web.shifts.WeekPlanOut
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.uuid.UUID
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}
fun Route.shifts() {
    val weekPlanService: WeekPlanService by inject()

    val principal = RfbpaPrincipal(
        RfbpaPrincipal.Subject("rohde"),
        RfbpaPrincipal.Name("rohde"),
        RfbpaPrincipal.Email("rohdef@rohdef.dk"),
        nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN),
    ).getOrElse { throw IllegalArgumentException("Foo") }

    get<Shifts.InInterval> {
        either {
            log.info { "Loading shifts for interval: ${it.yearWeekInterval}" }
//            val principal = call.rfbpaPrincipal().bind()


            val weekPlans = withError({ it.toApiError() }) {
                weekPlanService.shifts(principal, it.yearWeekInterval).bind()
            }

            weekPlans.map { WeekPlanOut.from(it) }.httpOk()
        }
    }

    get<Shift.ById> { iv ->
        either {
            log.info { iv.id }
            "Having fun ${iv.id}?".httpOk()


        }
    }
}

@Resource("/shifts")
class Shifts {
    @Resource("{yearWeekInterval}")
    class InInterval(
        val parent: Shifts = Shifts(),
        val yearWeekInterval: YearWeekInterval,
    )
}

@Resource("/shift")
class Shift {
    @Resource("{id}")
    class ById(
        val parent: Shift = Shift(),
        val id: UUID,
    )
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
