package dk.rohdef.helperplanning.shifts

import dk.rohdef.rfweeks.YearWeekDayAtTime

data class Shift(
    val helperBooking: HelperBooking,
    val shiftId: ShiftId,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
    val registrations: List<Registration> = emptyList(),
) {
    constructor(
        helperId: HelperBooking,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) : this(
        helperId,
        ShiftId.generateId(),
        start,
        end,
    )
}
