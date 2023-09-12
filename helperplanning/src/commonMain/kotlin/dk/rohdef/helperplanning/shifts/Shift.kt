package dk.rohdef.helperplanning.shifts

import kotlinx.datetime.LocalDateTime

data class Shift(
    val helperId: HelperBooking,
    val bookingId: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    companion object
}