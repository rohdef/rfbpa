package dk.rohdef.helperplanning.shifts

import dk.rohdef.rfweeks.YearWeek

data class WeekPlan(
    val week: YearWeek,

    val monday: List<Shift>,
    val tuesday: List<Shift>,
    val wednesday: List<Shift>,
    val thursday: List<Shift>,
    val friday: List<Shift>,
    val saturday: List<Shift>,
    val sunday: List<Shift>,
) {
    val allDays = listOf(
        monday,
        tuesday,
        wednesday,
        thursday,
        friday,
        saturday,
        sunday,
    )

    val allShifts =
        allDays.flatMap { it }
}
