package dk.rohdef.rfbpa.web.shifts

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ShiftOutput(
    val bookingId: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
)
