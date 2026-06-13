package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import dk.rohdef.rfweeks.YearWeekDayAtTime

data class Shift private constructor(
    val helperBooking: HelperBooking,
    val shiftId: ShiftId,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
    val registrations: List<Registration>,
    val references: List<Reference>,
) {
    companion object {
        fun createUnsafe(
            helperBooking: HelperBooking,
            shiftId: ShiftId,
            start: YearWeekDayAtTime,
            end: YearWeekDayAtTime,
            registrations: List<Registration>,
            references: List<Reference>,
        ): Shift = create(
            helperBooking,
            shiftId,
            start,
            end,
            registrations,
            references,
        ).getOrThrow()

        fun createUnsafe(
            booking: HelperBooking,
            start: YearWeekDayAtTime,
            end: YearWeekDayAtTime,
        ): Shift = create(
            booking,
            start,
            end,
        ).getOrThrow()

        private fun Either<ShiftError, Shift>.getOrThrow(): Shift = when (this) {
            is Either.Right -> this.value
            is Either.Left -> throw IllegalStateException("Validated Shift construction failed: ${this.value}")
        }

        fun Shift.copyUnsafe(
            helperBooking: HelperBooking = this.helperBooking,
            shiftId: ShiftId = this.shiftId,
            start: YearWeekDayAtTime = this.start,
            end: YearWeekDayAtTime = this.end,
            registrations: List<Registration> = this.registrations,
            references: List<Reference> = this.references,
        ): Shift = createUnsafe(
            helperBooking,
            shiftId,
            start,
            end,
            registrations,
            references,
        )

        fun create(
            helperBooking: HelperBooking,
            shiftId: ShiftId,
            start: YearWeekDayAtTime,
            end: YearWeekDayAtTime,
            registrations: List<Registration>,
            references: List<Reference>,
        ): Either<ShiftError, Shift> = either {
            ensure(start < end) {
                ShiftError.StartAfterEnd(start, end)
            }

            val hasIllnessRegistration = registrations.contains(Registration.Illness)
            ensure(!hasIllnessRegistration || helperBooking is HelperBooking.Booked) {
                ShiftError.IllnessRegistrationWithoutBooking
            }

            val hasFromIllnessReference = references
                .filterIsInstance<Reference.From>()
                .any() { it.linkType == Reference.LinkType.ILLNESS }
            ensure(!hasFromIllnessReference || hasIllnessRegistration) {
                ShiftError.IllnessReferenceWithoutIllnessRegistration
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
