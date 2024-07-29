package dk.rohdef.helperplanning.shifts

import arrow.core.*
import arrow.core.raise.either
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

class WeekPlanServiceImplementation(
    private val salarySystem: SalarySystemRepository,
    private val shiftRepository: ShiftRepository,
    private val weekSynchronizationRepository: WeekSynchronizationRepository,
) : WeekPlanService {
    override suspend fun synchronize(yearWeekInterval: YearWeekInterval): Either<NonEmptyList<SynchronizationError>, Unit> =
        either {
            val synchronizationStates = weekSynchronizationRepository.synchronizationStates(yearWeekInterval)
            val weeksToSynchronize = synchronizationStates
                .filterValues { it == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE }
                .keys
            weeksToSynchronize.mapOrAccumulate { synchronize(it).bind() }
                .bind()
        }

    override suspend fun synchronize(yearWeek: YearWeek): Either<SynchronizationError, Unit> = either {
        val synchronizationState = weekSynchronizationRepository.synchronizationState(yearWeek)
        if (synchronizationState == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE) {
            salarySystem.shifts(yearWeek)
                .flatMap { it.allShifts.mapOrAccumulate { shiftRepository.createShift(it).bind() } }
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()

            weekSynchronizationRepository.markSynchronized(yearWeek)
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()
        }
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) = either {
        // TODO mark possibly-synced
        // TODO improve domain errors (i.e., create them)
        weekSynchronizationRepository.markForSynchronization(start.yearWeek)
            .mapLeft { }
            .bind()

        val shift = salarySystem.createShift(start, end)
            // TODO try add shift repository
//            .flatMap { shiftRepository.createShift(it) }
            .bind()
        shift

        // TODO: 16/07/2024 rohdef
        // systemet detecter når vi booker - er det nok?
        // måske sync skal have strategi til conflict?

    }

    override suspend fun shifts(yearWeekInterval: YearWeekInterval): Either<WeekPlanServiceError, List<WeekPlan>> = either {
        synchronize(yearWeekInterval)
            .mapLeft {
                it.map {
                    when (it) {
                        is SynchronizationError.CouldNotSynchronizeWeek -> WeekPlanServiceError.AccessDeniedToSalarySystem
                    }
                }.first()
            }.bind()

        shiftRepository.shifts(yearWeekInterval)
            .mapLeft {
               WeekPlanServiceError.CannotCommunicateWithShiftsRepository
            }
            .bind()
    }
}
