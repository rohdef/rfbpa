package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
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
        ): Either<ShiftError, Shift> = either {
            ensure(start.localDateTime < end.localDateTime) {
                ShiftError.StartAfterEnd(start, end)
            }

            val hasIllnessRegistration = registrations.contains(Registration.Illness)
            val hasIllnessReference = references.any {
                it.linkType == Reference.LinkType.ILLNESS
            }
            ensure(!hasIllnessReference || hasIllnessRegistration) {
                ShiftError.IllnessReferenceWithoutIllnessRegistration
            }

            val isBooked = helperBooking is HelperBooking.Booked
            ensure(!hasIllnessRegistration || isBooked) {
                ShiftError.IllnessRegistrationWithoutBooking
            }

            Shift(
                helperBooking,
                shiftId,
                start,
                end,
                registrations,
                references,
            )
        }

        fun create(
            booking: HelperBooking,
            start: YearWeekDayAtTime,
            end: YearWeekDayAtTime,
        ): Either<ShiftError, Shift> = create(
            booking,
            ShiftId.generateId(),
            start,
            end,
            listOf(),
            listOf(),
        )
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
