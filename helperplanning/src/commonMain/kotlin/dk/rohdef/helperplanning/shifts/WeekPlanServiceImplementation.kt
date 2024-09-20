package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import arrow.core.raise.ensure
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
    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval
    ): Either<NonEmptyList<SynchronizationError>, Unit> =
        either {
            ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
                nonEmptyListOf(
                    SynchronizationError.InsufficientPermissions(
                        RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                        principal.roles,
                    )
                )
            }

            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(principal.subject, yearWeekInterval)
            val weeksToSynchronize = synchronizationStates
                .filterValues { it == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE }
                .keys
            weeksToSynchronize.mapOrAccumulate { synchronize(principal, it).bind() }
                .bind()
        }

    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeek: YearWeek
    ): Either<SynchronizationError, Unit> = either {
        ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
            SynchronizationError.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                principal.roles,
            )
        }

        val synchronizationState = weekSynchronizationRepository.synchronizationState(principal.subject, yearWeek)
        if (synchronizationState == WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE) {
            val salaryWeekPlan = salarySystem.shifts(principal.subject, yearWeek)
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
        ensure(principal.roles.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN)) {
            WeekPlanServiceError.InsufficientPermissions(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN, principal.roles)
        }

        // TODO: 19/08/2024 rohdef -  #41 improve domain errors (i.e., create them)
        when (weekSynchronizationRepository.synchronizationState(principal.subject, start.yearWeek)) {
            WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED ->
                weekSynchronizationRepository.markPossiblyOutOfDate(principal.subject, start.yearWeek)
                    .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }
                    .bind()

            WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE -> {}
            WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE -> {}
        }

        val shift = salarySystem.createShift(principal.subject, start, end)
            .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }
            .bind()

        shiftRepository.createOrUpdate(principal.subject, shift)
            .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }
            .bind()

        shift
    }

    override suspend fun shifts(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval
    ): Either<WeekPlanServiceError, List<WeekPlan>> = either {
        synchronize(principal, yearWeekInterval)
            .mapLeft {
                it.map {
                    when (it) {
                        is SynchronizationError.CouldNotSynchronizeWeek -> WeekPlanServiceError.AccessDeniedToSalarySystem
                        is SynchronizationError.InsufficientPermissions -> TODO()
                    }
                }.first()
            }.bind()

        shiftRepository.byYearWeekInterval(principal.subject, yearWeekInterval)
            .mapLeft {
                WeekPlanServiceError.CannotCommunicateWithShiftsRepository
            }
            .bind()
    }
}
