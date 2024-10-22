package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import dk.rohdef.helperplanning.MemorySalarySystemRepository
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.letValue
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
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

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek,
    ): Either<ShiftsError, WeekPlan> = either {
        _shiftsErrorRunners.map { it(yearWeek).bind() }
        memoryWeekPlanRepository.shifts(subject, yearWeek).bind()
    }

    override suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime, end: YearWeekDayAtTime,
    ): Either<ShiftsError, Shift> {
        val shiftId = generateTestShiftId(start, end)
        val shift = Shift(HelperBooking.NoBooking, shiftId, start, end)

        memoryWeekPlanRepository._shifts.letValue(subject) {
            it + (shiftId to shift)
        }

        return shift.right()
    }
}
