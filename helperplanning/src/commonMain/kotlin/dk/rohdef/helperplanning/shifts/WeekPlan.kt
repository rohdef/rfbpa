package dk.rohdef.helperplanning.shifts

data class WeekPlan(
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
        allDays
            .flatMap { it }
}
