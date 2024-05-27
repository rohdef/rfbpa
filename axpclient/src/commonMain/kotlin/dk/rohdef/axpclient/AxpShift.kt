package dk.rohdef.axpclient

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftType
import kotlinx.datetime.Instant

data class AxpShift(
    val start: Instant,
    val end: Instant,
    val helper: HelperBooking,
    val type: ShiftType,
) {
    data class CustomerId(val id: String)

    enum class ShiftType(
        val axpId: String
    ) {
        DAY("1"),
        EVENING("2"),
        NIGHT("3"),
        LONG("4"),
        HOURS_24("5");

        companion object {
            fun from(shiftType: dk.rohdef.helperplanning.shifts.ShiftType): ShiftType {
                return when (shiftType) {
                    dk.rohdef.helperplanning.shifts.ShiftType.DAY -> DAY
                    dk.rohdef.helperplanning.shifts.ShiftType.EVENING -> EVENING
                    dk.rohdef.helperplanning.shifts.ShiftType.NIGHT -> NIGHT
                    dk.rohdef.helperplanning.shifts.ShiftType.LONG -> LONG
                    dk.rohdef.helperplanning.shifts.ShiftType.HOURS_24 -> HOURS_24
                }
            }
        }
    }
}
