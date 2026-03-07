package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.DayOfWeek

internal fun YearWeek.shift(dayOfWeek: DayOfWeek): ShiftBuilderWithHelper {
    val atDayOfWeek = this.atDayOfWeek(dayOfWeek)

    fun createTestShift(helperBooking: HelperBooking, start: YearWeekDayAtTime, end: YearWeekDayAtTime): Shift {
        return Shift(
            helperBooking,
            TestSalarySystemRepository.IdGenerator.Default.generate(start, end),
            start,
            end,
        )
    }

    return object : ShiftBuilderWithHelper {
        override fun helper(helperBooking: HelperBooking): ShiftBuilderOnDay {
            return ShiftBuilderOnDay { startHours, startMinutes ->
                ShiftBuilderAtStart { endHours, endMinutes ->
                    createTestShift(
                        helperBooking,
                        atDayOfWeek.atTime(startHours, startMinutes),
                        atDayOfWeek.atTime(endHours, endMinutes),
                    )
                }
            }
        }

        override fun start(hours: Int, minutes: Int): ShiftBuilderAtStart =
            helper(HelperBooking.NoBooking).start(hours, minutes)
    }
}

internal interface ShiftBuilderWithHelper {
    fun helper(helperBooking: HelperBooking): ShiftBuilderOnDay
    fun start(hours: Int, minutes: Int): ShiftBuilderAtStart
}

internal fun interface ShiftBuilderOnDay {
    fun start(hours: Int, minutes: Int): ShiftBuilderAtStart
}

internal fun interface ShiftBuilderAtStart {
    fun end(hours: Int, minutes: Int): Shift
}

internal interface SalaryShiftBuilderWithHelper {
    fun helper(helperBooking: HelperBooking): SalaryShiftBuilderOnDay
    fun start(hours: Int, minutes: Int): SalaryShiftBuilderAtStart
}

internal fun interface SalaryShiftBuilderOnDay {
    fun start(hours: Int, minutes: Int): SalaryShiftBuilderAtStart
}

internal fun interface SalaryShiftBuilderAtStart {
    fun end(hours: Int, minutes: Int): SalaryShift
}