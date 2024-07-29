import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

val shiftIdNamespace = UUID("418de6bc-5185-4c40-ba2e-e1d9313dc1c0")

/**
 * This assumes no overlap in shift start/end pairs
 */
fun generateTestShiftId(start: YearWeekDayAtTime, end: YearWeekDayAtTime): ShiftId {
    val idText = "$start--$end"

    return ShiftId(
        UUID.generateUUID(shiftIdNamespace, idText)
    )
}
