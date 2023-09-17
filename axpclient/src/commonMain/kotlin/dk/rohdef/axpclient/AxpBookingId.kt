package dk.rohdef.axpclient

import dk.rohdef.helperplanning.shifts.BookingId

data class AxpBookingId(
    val axpId: String,
) {
    fun bookingId(): BookingId =
        BookingId(axpId)
}