package dk.rohdef.helperplanning.shifts

data class Weekday(
    val day: ShiftData,
    val evening: ShiftData,
    val night: ShiftData,
    val all24Hours: ShiftData,
    val long: ShiftData,

    val illness: ShiftData,
) {
    val allShifts =
        day + evening + night + all24Hours + long
}