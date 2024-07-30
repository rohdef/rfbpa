package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.WeekPlan
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

    fun Shift.book(helperBooking: HelperBooking): Shift = this.copy(helperBooking = helperBooking)

    val week29 = YearWeek(2024, 29)
    val week30 = YearWeek(2024, 30)
    val week31 = YearWeek(2024, 31)

    val week29To31 = week29..week31

    val shiftW29Wednesday1 = week29.shift(DayOfWeek.WEDNESDAY).start(11, 0).end(16, 30)
    val shiftW29Friday1 = week29.shift(DayOfWeek.FRIDAY).start(9, 45).end(17, 0)
    val weekPlanWeek29 = WeekPlan(
        week29,

        listOf(),
        listOf(),
        listOf(shiftW29Wednesday1),
        listOf(),
        listOf(shiftW29Friday1),
        listOf(),
        listOf(),
    )

    val shiftW30Tuesday1 = week30.shift(DayOfWeek.TUESDAY).start(7, 30).end(13, 0)
    val shiftW30Tuesday2 = week30.shift(DayOfWeek.TUESDAY).start(18, 15).end(22, 45)
    val shiftW30Saturday1 = week30.shift(DayOfWeek.SATURDAY).start(9, 0).end(23, 0)
    val weekPlanWeek30 = WeekPlan(
        week30,

        listOf(),
        listOf(
            shiftW30Tuesday1,
            shiftW30Tuesday2,
        ),
        listOf(),
        listOf(),
        listOf(),
        listOf(shiftW30Saturday1),
        listOf(),
    )

    val shiftW31Wednesday1 = week31.shift(DayOfWeek.WEDNESDAY).start(5, 45).end(21, 15)
    val shiftW31Sunday1 = week31.shift(DayOfWeek.SUNDAY).start(5, 45).end(21, 15)
    val weekPlanWeek31 = WeekPlan(
        week31,

        listOf(),
        listOf(),
        listOf(shiftW31Wednesday1),
        listOf(),
        listOf(),
        listOf(),
        listOf(shiftW31Sunday1),
    )

    internal object BookedShifts {

    }
}
