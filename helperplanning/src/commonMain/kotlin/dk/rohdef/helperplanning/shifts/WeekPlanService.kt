package dk.rohdef.helperplanning.shifts

import arrow.core.raise.either
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class WeekPlanService(
    private val salarySystem: SalarySystemRepository,
    private val shiftRepository: ShiftRepository,
) {
    suspend fun sync(yearWeekInterval: YearWeekInterval) = either {
        val q = salarySystem.cacheMisses(yearWeekInterval).bind()
        q.forEach {
            sync(it)
        }
    }

    suspend fun sync(yearWeek: YearWeek) = either {
        val w = salarySystem.shifts(yearWeek).bind()

        w.allShifts.forEach {
//            shiftRepository.createShift()
        }
    }

    suspend fun shifts(yearWeekInterval: YearWeekInterval) {
        sync(yearWeekInterval)

        val shifts = shiftRepository.shifts(yearWeekInterval)
    }
}
