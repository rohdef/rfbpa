package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.YearWeekDayAtTime

typealias ShiftsPrerunner = (yearWeek: YearWeek) -> Unit

class TestSalarySystemRepository(
    val memoryWeekPlanRepository: MemorySalarySystemRepository = MemorySalarySystemRepository(),
) : SalarySystemRepository by memoryWeekPlanRepository {
    private val _shiftsPrerunners = mutableListOf<ShiftsPrerunner>()
    fun addShiftsPrerunner(prerunner: ShiftsPrerunner) {
        _shiftsPrerunners.add(prerunner)
    }

    internal fun reset() = memoryWeekPlanRepository.reset()

    internal val shifts: Map<ShiftId, Shift>
        get() = memoryWeekPlanRepository.shifts
    internal val shiftList: List<Shift>
        get() = shifts.values.toList()
    internal val sortedByStartShifts: List<Shift>
        // TODO: 08/06/2024 rohdef - remove date conversion when implmenting comprable #4
        get() = shiftList.sortedBy { it.start.localDateTime }

    fun addShift(shift: Shift) {
        memoryWeekPlanRepository._shifts[shift.shiftId] = shift
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        _shiftsPrerunners.forEach { it(yearWeek) }
        return memoryWeekPlanRepository.shifts(yearWeek)
    }

    override suspend fun createShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Either<Unit, Shift> {
        val shiftId = generateTestShiftId(start, end)
        val shift = Shift(HelperBooking.NoBooking, shiftId, start, end)

        memoryWeekPlanRepository._shifts.put(shiftId, shift)

        return shift.right()
    }

    internal fun shiftListOnDay(yearWeekDay: YearWeekDay) =
        shiftList.filter { it.start.yearWeekDay == yearWeekDay }

    internal fun helpersOnDay(yearWeekDay: YearWeekDay): List<HelperBooking> {
        return shiftsOnDay(yearWeekDay).values
            .map { it.helperId }
    }

    internal fun shiftsOnDay(yearWeekDay: YearWeekDay): Map<ShiftId, Shift> {
        return shifts.filter { it.value.start.yearWeekDay == yearWeekDay }
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
