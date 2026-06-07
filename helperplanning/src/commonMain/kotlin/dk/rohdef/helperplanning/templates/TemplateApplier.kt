package dk.rohdef.helperplanning.templates

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftsService
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalTime

class TemplateApplier(
    private val shiftService: ShiftsService,
    private val helperRepository: HelpersRepository,
) {
    private val log = KotlinLogging.logger {}

    suspend fun applyTemplates(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval,
        // TODO: 21/09/2024 rohdef - change to NEL
        templates: List<Template>,
    ) : Either<Error, Unit> = either {
        ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN)) {
            Error.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN,
                principal.roles,
            )
        }

        // TODO: 08/06/2024 rohdef - temporary implementation due to expected method signature
        val schedulingStart = yearWeekInterval.start
        val schedulingEnd = yearWeekInterval.endInclusive

        val template = templates.first()
        if (template.start > schedulingEnd) {
            return Unit.right()// Template is for later than current scheduling
        }

        val weeksUntilTemplate = schedulingStart.weeksUntil(template.start)
        val indexAdjustmentModifier = -(weeksUntilTemplate % template.weeks.size)
        val indexAdjustment = maxOf(0, indexAdjustmentModifier)
        val templateStart = maxOf(template.start, schedulingStart)

        val allHelpers = helperRepository.all()
            .associate { it.shortName to it }
            .withDefault { TODO("Could not find helper with short name: $it") }

        log.info { "Applying template in interval ${templateStart}--${schedulingEnd}" }
        (templateStart..schedulingEnd).forEachIndexed { index, yearWeek ->
            val weekIndex = (index + indexAdjustment) % template.weeks.size
            val weekTemplate = template.weeks[weekIndex]
            applyWeekTemplates(principal, allHelpers, yearWeek, weekTemplate)
        }
    }

    private suspend fun applyWeekTemplates(principal: RfbpaPrincipal, allHelpers: Map<String, Helper>, week: YearWeek, weekTemplate: WeekTemplate) {
        log.info { "Creating shifts for week $week - using template ${weekTemplate.name}" }

        fun HelperReservation.bah(): HelperBooking {
            return when (this) {
                is HelperReservation.Helper -> HelperBooking.Booked(allHelpers.getValue(this.id).id)
                HelperReservation.NoReservation -> HelperBooking.NoBooking
            }
        }

        weekTemplate.shifts.forEach {
            val yearWeekDay = week.atDayOfWeek(it.key)

            it.value.forEach {
                val start = yearWeekDay.atTime(it.start)
                val end = start.untilTime(it.end)

                val shiftId = shiftService.createShift(
                    principal,
                    start,
                    end,
                    it.helper.bah(),
                )
                log.info { "\tcreated shift: ${start.week} ${start.dayOfWeek} ${start.time} -- ${end.time} with id ${shiftId}" }
            }
        }
    }

    private fun YearWeekDayAtTime.untilTime(time: LocalTime): YearWeekDayAtTime {
        val correctedDate = if (time < this.time) {
            this.yearWeekDay.nextDay()
        } else {
            this.yearWeekDay
        }

        return correctedDate.atTime(time)
    }

    sealed interface Error {
        data class InsufficientPermissions(
            val expectedRole: RfbpaPrincipal.RfbpaRoles,
            val actualRoles: Set<RfbpaPrincipal.RfbpaRoles>,
        ) : Error
    }
}
