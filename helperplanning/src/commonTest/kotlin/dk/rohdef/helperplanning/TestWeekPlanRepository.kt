package dk.rohdef.helperplanning

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeekDay

class TestWeekPlanRepository(
    val memoryWeekPlanRepository: MemoryWeekPlanRepository = MemoryWeekPlanRepository(),
) : WeekPlanRepository by memoryWeekPlanRepository {
    internal fun reset() = memoryWeekPlanRepository.reset()

    internal val shifts: Map<ShiftId, MemoryWeekPlanRepository.MemoryShift>
        get() = memoryWeekPlanRepository.shifts
    internal val shiftList: List<MemoryWeekPlanRepository.MemoryShift>
        get() = shifts.values.toList()
    internal val sortedByStartShifts: List<MemoryWeekPlanRepository.MemoryShift>
        // TODO: 08/06/2024 rohdef - remove date conversion when implmenting comprable #4
        get() = shiftList.sortedBy { it.start.localDateTime }

    internal fun shiftListOnDay(yearWeekDay: YearWeekDay) =
        shiftList.filter { it.start.yearWeekDay == yearWeekDay }

    internal fun helpersOnDay(yearWeekDay: YearWeekDay): List<HelperBooking> {
        return shiftsOnDay(yearWeekDay).keys
            .map { memoryWeekPlanRepository.bookings.getValue(it) }
    }

    internal fun shiftsOnDay(yearWeekDay: YearWeekDay): Map<ShiftId, MemoryWeekPlanRepository.MemoryShift> {
        return  shifts.filter { it.value.start.yearWeekDay == yearWeekDay }
    }

    internal fun firstShiftStart(): YearWeekDay {
        return this.sortedByStartShifts
            .first()
            .start
            .yearWeekDay
    }

    internal fun lastShiftStart(): YearWeekDay {
        return this.sortedByStartShifts
            .last()
            .start
            .yearWeekDay
    }
}
