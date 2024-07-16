package dk.rohdef.helperplanning.shifts

import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

data class Shift(
    val helperId: HelperBooking,
    val bookingId: UUID,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
) {
    constructor(
        helperId: HelperBooking,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) : this(
        helperId,
        // TODO: 25/06/2024 rohdef - should be handled by stronger types
        UUID.generateUUID(),
        start,
        end,
    )
}
