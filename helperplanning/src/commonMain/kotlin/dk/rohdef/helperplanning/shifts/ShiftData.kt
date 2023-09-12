package dk.rohdef.helperplanning.shifts

import arrow.core.NonEmptyList

sealed interface ShiftData {
    operator fun plus(shiftData: ShiftData): ShiftData

    object NoData : ShiftData {
        override operator fun plus(shiftData: ShiftData): ShiftData =
            shiftData
    }

    data class Shifts(val shifts: NonEmptyList<Shift>) : ShiftData {
        override operator fun plus(shiftData: ShiftData): Shifts {
            return when (shiftData) {
                is NoData -> this
                is Shifts -> Shifts(this.shifts + shiftData.shifts)
            }
        }
    }
}