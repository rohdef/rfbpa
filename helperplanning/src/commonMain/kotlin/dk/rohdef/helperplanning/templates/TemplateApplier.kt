package dk.rohdef.helperplanning.templates

import arrow.core.Either
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.rfweeks.YearWeek
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.*

class TemplateApplier(
    val weekPlanRepository: WeekPlanRepository,
) {
    private val log = KotlinLogging.logger {}

    suspend fun applyTemplate(
        schedulingStart: YearWeek,
        schedulingEndExclusive: YearWeek,
        template: Template,
    ) {
        val templateStart = maxOf(template.start, schedulingStart)
        // TODO date of next template
        val templateEnd = schedulingEndExclusive.previousWeek()
        log.info { "Applying template in interval ${templateStart}--${templateEnd}" }

        (templateStart..templateEnd).forEach {
            // TODO deal with looping templates!
            applyWeekTemplates(it, template.weeks.first())
        }
    }

    private suspend fun applyWeekTemplates(week: YearWeek, weekTemplate: WeekTemplate) {
        log.info { "Creating shifts for week $week - using template ${weekTemplate.name}" }

        weekTemplate.shifts.forEach {
            val yearWeekDay = week.atDayOfWeek(it.key)
            val localDate = yearWeekDay.date

            it.value.forEach {
                val start = localDate.atTime(it.start)
                val end = start.untilTime(it.end)

                // TODO: 02/06/2024 rohdef - deal with time zones somehow, not fond of hard code
                val startInstant = start.toInstant(TimeZone.of("Europe/Copenhagen"))
                val endInstant = end.toInstant(TimeZone.of("Europe/Copenhagen"))

                val shiftId = weekPlanRepository.createShift(startInstant, endInstant, it.type)
                log.info { "\tcreated shift: ${start}--${end}" }

                when (shiftId) {
                    is Either.Right -> {
                        bookHelper(shiftId.value, it.helper)
                    }
                    is Either.Left -> {
                        log.error { "Could not book ${shiftId.value}" }
                    }
                }
            }
        }
    }

    private suspend fun bookHelper(shiftId: ShiftId, helper: HelperReservation) {
    }

    private fun LocalDateTime.untilTime(time: LocalTime): LocalDateTime {
        val correctedDate = if (time < this.time) {
            this.date.plus(1, DateTimeUnit.DAY)
        } else {
            this.date
        }

        return correctedDate.atTime(time)
    }
}
