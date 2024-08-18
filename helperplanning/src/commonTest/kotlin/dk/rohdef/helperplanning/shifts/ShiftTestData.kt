package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.DayOfWeek

object ShiftTestData {
    val year2024Week8 = YearWeek(2024, 8)
    val year2024Week9 = YearWeek(2024, 9)
    val year2024Week10 = YearWeek(2024, 10)

    object Fiktivus {
        val week8Shift1 = year2024Week8.shift(DayOfWeek.MONDAY).start(13, 30).end(14, 30)
        val week8Shift2 = year2024Week8.shift(DayOfWeek.WEDNESDAY).start(17, 30).end(21, 30)
        val week8Shifts = listOf(week8Shift1, week8Shift2)

        val week9Shift1 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(19, 15)
        val week9Shifts = listOf(week9Shift1)

        val week10Shift1 = year2024Week10.shift(DayOfWeek.WEDNESDAY).start(10, 45).end(20, 30)
        val week10Shift2 = year2024Week10.shift(DayOfWeek.SUNDAY).start(6, 0).end(23, 0)
        val week10Shifts = listOf(week10Shift1, week10Shift2)

        val allShiftsInSystem = week8Shifts + week9Shifts + week10Shifts

        val week8ShiftNotInSystem = year2024Week8.shift(DayOfWeek.SATURDAY).start(8, 0).end(15, 45)
        val week9ShiftNotInSystem = year2024Week9.shift(DayOfWeek.MONDAY).start(21, 0).end(22, 45)
        val week10ShiftNotInSystem = year2024Week10.shift(DayOfWeek.THURSDAY).start(8, 0).end(15, 45)

        val shiftsNotInSystem = listOf(week8ShiftNotInSystem, week9ShiftNotInSystem, week10ShiftNotInSystem)
    }

    object Realis {
        val week8Shift1 = year2024Week8.shift(DayOfWeek.TUESDAY).start(8, 30).end(23, 30)
        val week8Shift2 = year2024Week8.shift(DayOfWeek.WEDNESDAY).start(17, 30).end(21, 30)
        val week8Shifts = listOf(week8Shift1, week8Shift2)

        val week9Shift1 = year2024Week9.shift(DayOfWeek.MONDAY).start(8, 0).end(23, 15)
        val week9Shift2 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(23, 15)
        val week9Shift3 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(23, 15)
        val week9Shift4 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(23, 15)
        val week9Shift5 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(23, 15)
        val week9Shift6 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(23, 15)
        val week9Shift7 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(23, 15)
        val week9Shifts = listOf(week9Shift1, week9Shift2, week9Shift3, week9Shift4, week9Shift5, week9Shift6, week9Shift7)

        val week10Shift1 = year2024Week10.shift(DayOfWeek.WEDNESDAY).start(10, 45).end(20, 30)
        val week10Shift2 = year2024Week10.shift(DayOfWeek.SUNDAY).start(6, 0).end(23, 0)
        val week10Shifts = listOf(week10Shift1, week10Shift2)

        val allShiftsInSystem = week8Shifts + week9Shifts + week10Shifts

        val week8ShiftNotInSystem = year2024Week8.shift(DayOfWeek.SATURDAY).start(8, 0).end(15, 45)
        val week9ShiftNotInSystem = year2024Week9.shift(DayOfWeek.MONDAY).start(21, 0).end(22, 45)
        val week10ShiftNotInSystem = year2024Week10.shift(DayOfWeek.THURSDAY).start(8, 0).end(15, 45)

        val shiftsNotInSystem = listOf(week8ShiftNotInSystem, week9ShiftNotInSystem, week10ShiftNotInSystem)
    }
}

internal fun YearWeek.shift(dayOfWeek: DayOfWeek): ShiftBuilderOnDay {
    fun createTestShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Shift {
        return Shift(
            HelperBooking.NoBooking,
            generateTestShiftId(start, end),
            start,
            end,
        )
    }

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
