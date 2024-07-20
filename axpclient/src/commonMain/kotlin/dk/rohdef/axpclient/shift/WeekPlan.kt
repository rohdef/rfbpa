package dk.rohdef.axpclient.shift

internal data class WeekPlan(
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
            .flatMap { it.allShifts }
}
