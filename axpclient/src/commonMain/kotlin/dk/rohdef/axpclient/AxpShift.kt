package dk.rohdef.axpclient

import dk.rohdef.helperplanning.shifts.HelperBooking
import kotlinx.datetime.Instant

data class AxpShift(
    val start: Instant,
    val end: Instant,
    val helper: HelperBooking,
    val type: ShiftType,
) {
    data class CustomerId(val id: String)

    enum class ShiftType(
        val axpId: String
    ) {
        DAY("1"),
        EVENING("2"),
        NIGHT("3"),
        LONG("4"),
        HOURS_24("5");
    }
}
