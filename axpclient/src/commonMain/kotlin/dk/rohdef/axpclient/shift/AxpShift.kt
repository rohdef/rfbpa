package dk.rohdef.axpclient.shift

import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.helper.AxpMetadataRepository
import kotlinx.datetime.LocalDateTime

internal data class AxpShift(
    val axpHelperBooking: AxpMetadataRepository,
    val bookingId: AxpBookingId,
    val start: LocalDateTime,
    val end: LocalDateTime,
)
