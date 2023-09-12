package dk.rohdef.helperplanning.shifts

data class WeekPlan(
    val monday: Weekday,
    val tuesday: Weekday,
    val wednesday: Weekday,
    val thursday: Weekday,
    val friday: Weekday,
    val saturday: Weekday,
    val sunday: Weekday,
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
            .map { it.allShifts }
            .fold(ShiftData.NoData as ShiftData) { accumulator, shiftData ->
                accumulator + shiftData
            }

    companion object
}