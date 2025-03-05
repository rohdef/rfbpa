@file:OptIn(ExperimentalUuidApi::class)

import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.generateUuid
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val shiftIdNamespace = Uuid.parse("418de6bc-5185-4c40-ba2e-e1d9313dc1c0")

/**
 * This assumes no overlap in shift start/end pairs
 */
fun generateTestShiftId(start: YearWeekDayAtTime, end: YearWeekDayAtTime): ShiftId {
    val idText = "$start--$end"

    return ShiftId(
        Uuid.generateUuid(shiftIdNamespace, idText)
    )
}