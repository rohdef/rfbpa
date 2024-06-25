package dk.rohdef.helperplanning.shifts

data class Weekday(
    val day: List<Shift>,
    val evening: List<Shift>,
    val night: List<Shift>,
    val all24Hours: List<Shift>,
    val long: List<Shift>,

    val illness: List<Shift>,
) {
    val allShifts =
        day + evening + night + all24Hours + long
}
