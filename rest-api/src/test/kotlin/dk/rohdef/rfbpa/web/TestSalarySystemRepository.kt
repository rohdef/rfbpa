package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import dk.rohdef.helperplanning.MemorySalarySystemRepository
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.YearWeekDayAtTime
import generateTestShiftId

typealias SalaryShiftsErrorRunner = (yearWeek: YearWeek) -> Either<ShiftsError, Unit>

class TestSalarySystemRepository(
    val memoryWeekPlanRepository: MemorySalarySystemRepository = MemorySalarySystemRepository(),
) : SalarySystemRepository by memoryWeekPlanRepository {
    private val _shiftsErrorRunners = mutableListOf<SalaryShiftsErrorRunner>()
    fun addShiftsErrorRunner(errorRunner: SalaryShiftsErrorRunner) {
        _shiftsErrorRunners.add(errorRunner)
    }

    internal fun reset() {
        memoryWeekPlanRepository.reset()
        _shiftsErrorRunners.clear()
    }

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

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> = either {
        _shiftsErrorRunners.map { it(yearWeek).bind() }
        memoryWeekPlanRepository.shifts(yearWeek).bind()
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
