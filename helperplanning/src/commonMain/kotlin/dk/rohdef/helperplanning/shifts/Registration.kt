package dk.rohdef.helperplanning.shifts

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import kotlinx.datetime.LocalDateTime

sealed interface Registration {
    data class Illness(
        val timeOfRegistration: LocalDateTime,
        val replacementShiftId: Option<ShiftId>,
    ): Registration {
        constructor(timeOfRegistration: LocalDateTime) :
                this(timeOfRegistration, none())

        constructor(timeOfRegistration: LocalDateTime, replacementShiftId: ShiftId) :
                this(timeOfRegistration, replacementShiftId.some())
    }

    data class IllnessReplacement(
        val forIllnessShift: ShiftId,
    ): Registration
}