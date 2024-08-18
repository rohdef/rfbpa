package dk.rohdef.axpclient.shift

internal sealed interface ShiftData {
    operator fun plus(shiftData: ShiftData): ShiftData

    object NoData : ShiftData {
        override operator fun plus(shiftData: ShiftData): ShiftData =
            shiftData
    }

    data class Shifts(val axpShifts: List<AxpShift>) : ShiftData {
        override operator fun plus(shiftData: ShiftData): Shifts {
            return when (shiftData) {
                NoData -> this
                is Shifts -> Shifts(this.axpShifts + shiftData.axpShifts)
            }
        }
    }
}
