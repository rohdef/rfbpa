package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.rfweeks.YearWeek

class TemplateApplication(
    val weekPlanRepository: WeekPlanRepository,
) {
    suspend fun performThisBitch(
        schedulingStart: YearWeek,
        schedulingEnd: YearWeek,
        templates: List<Template>,
    ) {

    }
}
