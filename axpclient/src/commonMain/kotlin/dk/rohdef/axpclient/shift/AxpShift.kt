package dk.rohdef.axpclient.shift

import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.helper.AxpHelperBooking
import kotlinx.datetime.LocalDateTime

internal data class AxpShift(
    val axpHelperBooking: AxpHelperBooking,
    val bookingId: AxpBookingId,
    val start: LocalDateTime,
    val end: LocalDateTime,
)
