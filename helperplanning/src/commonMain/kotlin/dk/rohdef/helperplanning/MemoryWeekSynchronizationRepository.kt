package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    internal val synchronizedWeeks = mutableMapOf<YearWeek, WeekSynchronizationRepository.SynchronizationState>().withDefault { WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE }

    fun reset() {
        synchronizedWeeks.clear()
    }

    override fun markForSynchronization(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks.remove(yearWeek)
            return Unit.right()
    }

    override fun markPossiblyOutOfDate(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks[yearWeek] = WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE
        return Unit.right()
    }

    override fun markSynchronized(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks[yearWeek] = WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        return Unit.right()
    }

    override fun synchronizationStates(subject: RfbpaPrincipal.Subject, yearWeekInterval: YearWeekInterval): Map<YearWeek, WeekSynchronizationRepository.SynchronizationState> {
        return yearWeekInterval.associate { it to synchronizationState(subject, it) }
    }

    override fun synchronizationState(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): WeekSynchronizationRepository.SynchronizationState {
        return synchronizedWeeks.getOrDefault(yearWeek, WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)
    }
}
