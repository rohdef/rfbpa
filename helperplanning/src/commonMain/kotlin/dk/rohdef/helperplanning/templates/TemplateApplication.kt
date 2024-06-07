package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.rfweeks.YearWeek
import kotlinx.datetime.*

class TemplateApplication(
    val weekPlanRepository: WeekPlanRepository,
) {
    suspend fun applyTemplate(
        schedulingStart: YearWeek,
        schedulingEndExclusive: YearWeek,
        templates: Template,
    ) {

    }

    private suspend fun applyWeekTemplates(week: YearWeek, weekTemplates: List<WeekTemplate>) {
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
