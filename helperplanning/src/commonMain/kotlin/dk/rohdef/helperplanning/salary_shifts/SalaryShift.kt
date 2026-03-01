package dk.rohdef.helperplanning.salary_shifts

import dk.rohdef.helperplanning.shifts.Registration
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeekDayAtTime

data class SalaryShift(
    val helperBooking: SalaryBooking,
    val shiftId: ShiftId,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
    val registrations: List<Registration> = emptyList(),
) {
    constructor(
        helperBooking: SalaryBooking,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) : this(
        helperBooking,
        ShiftId.generateId(),
        start,
        end,
    )
}
