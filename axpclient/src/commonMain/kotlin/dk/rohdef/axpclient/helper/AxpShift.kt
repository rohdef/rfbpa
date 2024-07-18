package dk.rohdef.axpclient.helper

import dk.rohdef.axpclient.AxpBookingId
import kotlinx.datetime.LocalDateTime

internal data class AxpShift(
    val axpHelperBooking: AxpMetadataRepository,
    val bookingId: AxpBookingId,
    val start: LocalDateTime,
    val end: LocalDateTime,
)
