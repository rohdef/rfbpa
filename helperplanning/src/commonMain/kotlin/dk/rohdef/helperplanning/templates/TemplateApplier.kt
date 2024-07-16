package dk.rohdef.helperplanning.templates

import arrow.core.Either
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalTime

class TemplateApplier(
    val salarySystemRepository: SalarySystemRepository,
    private val helpers: Map<String, Helper.ID>,
) {
    private val log = KotlinLogging.logger {}

    suspend fun applyTemplates(
        yearWeekInterval: YearWeekInterval,
        templates: List<Template>,
    ) {
        // TODO: 08/06/2024 rohdef - temporary implementation due to expected method signature
        val schedulingStart: YearWeek = yearWeekInterval.start
        val schedulingEnd: YearWeek = yearWeekInterval.endInclusive
        applyTemplate(schedulingStart, schedulingEnd, templates.first())
    }

    suspend fun applyTemplate(
        schedulingStart: YearWeek,
        schedulingEnd: YearWeek,
        template: Template,
    ) {
        if (template.start > schedulingEnd) {
            return // Template is for later than current scheduling
        }

        val weeksUntilTemplate = schedulingStart.weeksUntil(template.start)
        val indexAdjustmentModifier = -(weeksUntilTemplate % template.weeks.size)
        val indexAdjustment = maxOf(0, indexAdjustmentModifier)
        val templateStart = maxOf(template.start, schedulingStart)

        log.info { "Applying template in interval ${templateStart}--${schedulingEnd}" }
        (templateStart..schedulingEnd).forEachIndexed { index, yearWeek ->
            val weekIndex = (index + indexAdjustment) % template.weeks.size
            val weekTemplate = template.weeks[weekIndex]
            applyWeekTemplates(yearWeek, weekTemplate)
        }
    }

    private suspend fun applyWeekTemplates(week: YearWeek, weekTemplate: WeekTemplate) {
        log.info { "Creating shifts for week $week - using template ${weekTemplate.name}" }

        weekTemplate.shifts.forEach {
            val yearWeekDay = week.atDayOfWeek(it.key)

            it.value.forEach {
                val start = yearWeekDay.atTime(it.start)
                val end = start.untilTime(it.end)

                val shiftId = salarySystemRepository.createShift(start, end)
                log.info { "\tcreated shift: ${start.week} ${start.dayOfWeek} ${start.time} -- ${end.time}" }

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

    // TODO: 25/06/2024 rohdef - this probably needs a bit of rework
    private suspend fun bookHelper(shiftId: ShiftId, helperReservation: HelperReservation) {
        when (helperReservation) {
            is HelperReservation.Helper -> {
                val helper = HelperBooking.PermanentHelper(helpers[helperReservation.id]!!)

                val bookingId = salarySystemRepository.bookShift(
                    shiftId,
                    helper.helperId,
                )

                when (bookingId) {
                    is Either.Right -> log.info { "Successfully booked ${helper}" }
                    is Either.Left -> log.error { "Could not book helper for shift ${shiftId}" }
                }
            }

            HelperReservation.NoReservation -> log.info { "No helper specified" }
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
}
