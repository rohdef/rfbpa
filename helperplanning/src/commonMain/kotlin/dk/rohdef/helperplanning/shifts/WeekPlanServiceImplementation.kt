package dk.rohdef.helperplanning.shifts

import arrow.core.*
import arrow.core.raise.either
import dk.rohdef.helperplanning.RfbpaPrincipal
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
    override suspend fun synchronize(principal: RfbpaPrincipal, yearWeekInterval: YearWeekInterval): Either<NonEmptyList<SynchronizationError>, Unit> =
        either {
            val synchronizationStates = weekSynchronizationRepository.synchronizationStates(principal.subject, yearWeekInterval)
            val weeksToSynchronize = synchronizationStates
                .filterValues { it == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE }
                .keys
            weeksToSynchronize.mapOrAccumulate { synchronize(principal, it).bind() }
                .bind()
        }

    override suspend fun synchronize(principal: RfbpaPrincipal, yearWeek: YearWeek): Either<SynchronizationError, Unit> = either {
        val synchronizationState = weekSynchronizationRepository.synchronizationState(principal.subject, yearWeek)
        if (synchronizationState == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE) {
            val salaryWeekPlan = salarySystem.shifts(yearWeek)
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()

            salaryWeekPlan.allShifts.mapOrAccumulate { shiftRepository.createOrUpdate(principal.subject, it).bind() }
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()

            weekSynchronizationRepository.markSynchronized(principal.subject, yearWeek)
                .mapLeft { SynchronizationError.CouldNotSynchronizeWeek(yearWeek) }
                .bind()
        }
    }

    override suspend fun createShift(
        principal: RfbpaPrincipal,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ) = either {
        // TODO mark possibly-synced
        // TODO improve domain errors (i.e., create them)
        weekSynchronizationRepository.markForSynchronization(principal.subject, start.yearWeek)
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

    override suspend fun shifts(principal: RfbpaPrincipal, yearWeekInterval: YearWeekInterval): Either<WeekPlanServiceError, List<WeekPlan>> = either {
        synchronize(principal, yearWeekInterval)
            .mapLeft {
                it.map {
                    when (it) {
                        is SynchronizationError.CouldNotSynchronizeWeek -> WeekPlanServiceError.AccessDeniedToSalarySystem
                    }
                }.first()
            }.bind()

        shiftRepository.byYearWeekInterval(yearWeekInterval)
            .mapLeft {
               WeekPlanServiceError.CannotCommunicateWithShiftsRepository
            }
            .bind()
    }
}
