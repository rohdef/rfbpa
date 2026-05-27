package dk.rohdef.axpclient.shift

import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.helper.AxpHelperBooking
import dk.rohdef.axpclient.helper.AxpIllnessBooking
import kotlinx.datetime.LocalDateTime

internal data class AxpIllnessShift(
    val axpHelperBooking: AxpIllnessBooking,
    val bookingId: AxpBookingId,
    val start: LocalDateTime,
    val end: LocalDateTime,
)
