package dk.rohdef.helperplanning.shifts

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import kotlinx.datetime.LocalDateTime

sealed interface Registration {
    data class Illness(
        val replacementShiftId: Option<ShiftId>,
    ): Registration {
        constructor() :
                this(none())

        constructor(shiftId: ShiftId) :
                this(shiftId.some())
    }

    data class IllnessReplacement(
        val forIllnessShift: ShiftId,
    ): Registration
}