package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import dk.rohdef.rfweeks.YearWeekDayAtTime

data class Shift(
    val helperBooking: HelperBooking,
    val shiftId: ShiftId,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
    val registrations: List<Registration>,
    val references: List<Reference>,
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
        listOf(),
        listOf(),
    )

    companion object {
        fun create(
            helperBooking: HelperBooking,
            shiftId: ShiftId,
            start: YearWeekDayAtTime,
            end: YearWeekDayAtTime,
            registrations: List<Registration>,
            references: List<Reference>,
        ): Either<ShiftError, Shift> = TODO()

        fun create(
            booking: HelperBooking,
            start: YearWeekDayAtTime,
            end: YearWeekDayAtTime,
        ): Either<ShiftError, Shift> = TODO()
    }

    sealed interface ShiftError {
        data class StartAfterEnd(
            val start: YearWeekDayAtTime,
            val end: YearWeekDayAtTime,
        ) : ShiftError

        data object IllnessReferenceWithoutIllnessRegistration : ShiftError
        data object IllnessRegistrationWithoutBooking : ShiftError
    }
}
