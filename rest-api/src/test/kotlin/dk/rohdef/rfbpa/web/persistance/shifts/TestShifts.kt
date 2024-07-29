package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.DayOfWeek
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

internal fun interface ShiftBuilderOnDay {
    fun start(hours: Int, minutes: Int): ShiftBuilderAtStart
}

internal fun interface ShiftBuilderAtStart {
    fun end(hours: Int, minutes: Int): Shift
}

internal object TestShifts {
    internal val shiftIdNamespace = UUID("ffe95790-1bc3-4283-8988-7c16809ac47d")

    /**
     * This assumes no overlap in shift start/end pairs
     */
    internal fun generateTestShiftId(start: YearWeekDayAtTime, end: YearWeekDayAtTime): ShiftId {
        val idText = "$start--$end"

        return ShiftId(
            UUID.generateUUID(shiftIdNamespace, idText)
        )
    }

    internal fun createTestShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Shift {
        return Shift(
            HelperBooking.NoBooking,
            generateTestShiftId(start, end),
            start,
            end,
        )
    }

    internal fun YearWeek.shift(dayOfWeek: DayOfWeek): ShiftBuilderOnDay {
        return ShiftBuilderOnDay { startHours, startMinutes ->
            ShiftBuilderAtStart { endHours, endMinutes ->
                this.atDayOfWeek(dayOfWeek).let {
                    createTestShift(
                        it.atTime(startHours, startMinutes),
                        it.atTime(endHours, endMinutes),
                    )
                }
            }
        }
    }

    val week29 = YearWeek(2024, 29)
    val week30 = YearWeek(2024, 30)
    val week31 = YearWeek(2024, 31)

    val week29To31 = week29..week31

    val shiftW29Wednesday1: Shift = week29.shift(DayOfWeek.WEDNESDAY).start(11, 0).end(16, 30)
}
