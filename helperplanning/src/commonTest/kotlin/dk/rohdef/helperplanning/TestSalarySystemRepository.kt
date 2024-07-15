package dk.rohdef.helperplanning

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeekDay

class TestSalarySystemRepository(
    val memoryWeekPlanRepository: MemorySalarySystemRepository = MemorySalarySystemRepository(),
) : SalarySystemRepository by memoryWeekPlanRepository {
    internal fun reset() = memoryWeekPlanRepository.reset()

    internal val shifts: Map<ShiftId, MemorySalarySystemRepository.MemoryShift>
        get() = memoryWeekPlanRepository.shifts
    internal val shiftList: List<MemorySalarySystemRepository.MemoryShift>
        get() = shifts.values.toList()
    internal val sortedByStartShifts: List<MemorySalarySystemRepository.MemoryShift>
        // TODO: 08/06/2024 rohdef - remove date conversion when implmenting comprable #4
        get() = shiftList.sortedBy { it.start.localDateTime }

    internal fun shiftListOnDay(yearWeekDay: YearWeekDay) =
        shiftList.filter { it.start.yearWeekDay == yearWeekDay }

    internal fun helpersOnDay(yearWeekDay: YearWeekDay): List<HelperBooking> {
        return shiftsOnDay(yearWeekDay).keys
            .map { memoryWeekPlanRepository.bookings.getValue(it) }
    }

    internal fun shiftsOnDay(yearWeekDay: YearWeekDay): Map<ShiftId, MemorySalarySystemRepository.MemoryShift> {
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
