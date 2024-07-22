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
    suspend fun synchronize(yearWeekInterval: YearWeekInterval) {
        val synchronizationStates = weekSynchronizationRepository.synchronizationStates(yearWeekInterval)
        val weeksToSynchronize = synchronizationStates
            .filterValues { it == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE }
            .keys
        weeksToSynchronize.forEach { synchronize(it) }
    }

    suspend fun synchronize(yearWeek: YearWeek) = either {
        val synchronizationState = weekSynchronizationRepository.synchronizationState(yearWeek)
        if (synchronizationState == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE) {
            val salaryWeeks = salarySystem.shifts(yearWeek)
                .mapLeft { TODO() }
                .bind()

            salaryWeeks.allShifts.map {
                shiftRepository.createShift(it)
                    .mapLeft { TODO() }
                    .bind()
            }

            weekSynchronizationRepository.markSynchronized(yearWeek)
        }
    }

    suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) = either {
        // TODO mark possibly-synced
        weekSynchronizationRepository.markForSynchronization(start.yearWeek)

        val shift = salarySystem.createShift(start, end).bind()
        // TODO try add shift repository
//        val x = shiftRepository.createShift(shift1Start, shift1End)
        //
        shift

        // TODO: 16/07/2024 rohdef
        // systemet detecter når vi booker - er det nok?
        // måske sync skal have strategi til conflict?

    }

    suspend fun shifts(yearWeekInterval: YearWeekInterval) {
        synchronize(yearWeekInterval)

        val shifts = shiftRepository.shifts(yearWeekInterval)
    }
}
