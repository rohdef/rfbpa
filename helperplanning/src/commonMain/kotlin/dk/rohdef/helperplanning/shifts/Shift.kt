package dk.rohdef.helperplanning.shifts

import dk.rohdef.rfweeks.YearWeekDayAtTime

data class Shift(
    val helperBooking: HelperBooking,
    val shiftId: ShiftId,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
    val registrations: List<Registration> = emptyList(),
    val references: List<Reference> = emptyList(),
) {
    constructor(
        booking: HelperBooking,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) : this(
        booking,
        ShiftId.generateId(),
        start,
        end,
    )
}
