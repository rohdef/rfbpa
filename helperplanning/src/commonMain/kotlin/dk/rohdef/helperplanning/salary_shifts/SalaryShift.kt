package dk.rohdef.helperplanning.salary_shifts

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeekDayAtTime

data class SalaryShift(
    val helperBooking: SalaryBooking,
    val shiftId: ShiftId,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
    // TODO - we probably want salary registratio
    val registrations: List<SalaryRegistration> = emptyList(),
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

    suspend fun toShift(existingBooking: suspend () -> Either<Unit, HelperId>): Either<Unit, Shift> = either {
        Shift(
            helperBooking.toBooking(existingBooking).bind(),
            shiftId,
            start,
            end,
            registrations.map { it.toRegistration() },
        )
    }
}
