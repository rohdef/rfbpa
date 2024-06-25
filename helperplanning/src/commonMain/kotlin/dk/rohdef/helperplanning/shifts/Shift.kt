package dk.rohdef.helperplanning.shifts

import kotlinx.datetime.LocalDateTime
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

data class Shift(
    val helperId: HelperBooking,
    val bookingId: UUID,
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    constructor(
        helperId: HelperBooking,
        start: LocalDateTime,
        end: LocalDateTime,
    ) : this(
        helperId,
        // TODO: 25/06/2024 rohdef - should be handled by stronger types
        UUID.generateUUID(),
        start,
        end,
    )
}
