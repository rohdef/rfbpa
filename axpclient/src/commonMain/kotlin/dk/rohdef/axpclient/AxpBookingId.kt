package dk.rohdef.axpclient

import dk.rohdef.helperplanning.shifts.ShiftId

data class AxpBookingId(
    val axpId: String,
) {
    fun bookingId(): ShiftId =
        ShiftId(axpId)
}
