package dk.rohdef.helperplanning.shifts

import arrow.core.raise.either
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

class WeekPlanService(
    private val salarySystem: SalarySystemRepository,
    private val shiftRepository: ShiftRepository,
    private val weekSynchronizationRepository: WeekSynchronizationRepository,
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

    suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) {
        // TODO mark week unsynced

//        val y = salarySystem.createShift(shift1Start, shift1End)

        // TODO: 16/07/2024 rohdef - how do we detect semi synced?
        // systemet detecter når vi booker - er det nok?
        // skal der evt. laves et sync, just in case? Måske smart nok
        // måske sync skal have strategi til conflict?
        // altid salary repository først, alt andet kan give problematiske fejl!

//        val x = shiftRepository.createShift(shift1Start, shift1End)
    }

    suspend fun shifts(yearWeekInterval: YearWeekInterval) {
        sync(yearWeekInterval)

        val shifts = shiftRepository.shifts(yearWeekInterval)
    }
}
