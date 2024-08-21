package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    internal val synchronizedWeeks = mutableMapOf<RfbpaPrincipal.Subject, MutableMap<YearWeek, WeekSynchronizationRepository.SynchronizationState>>()
        .withDefault { mutableMapOf<YearWeek, WeekSynchronizationRepository.SynchronizationState>().withDefault { WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE } }

    fun reset() {
        synchronizedWeeks.clear()
    }

    override fun markForSynchronization(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks.getValue(subject).remove(yearWeek)
            return Unit.right()
    }

    override fun markPossiblyOutOfDate(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks[subject] = synchronizedWeeks.getValue(subject)
        synchronizedWeeks.getValue(subject)[yearWeek] = WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE
        return Unit.right()
    }

    override fun markSynchronized(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks[subject] = synchronizedWeeks.getValue(subject)
        synchronizedWeeks.getValue(subject)[yearWeek] = WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        return Unit.right()
    }

    override fun synchronizationStates(subject: RfbpaPrincipal.Subject, yearWeekInterval: YearWeekInterval): Map<YearWeek, WeekSynchronizationRepository.SynchronizationState> {
        return yearWeekInterval.associate { it to synchronizationState(subject, it) }
    }

    override fun synchronizationState(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): WeekSynchronizationRepository.SynchronizationState {
        return synchronizedWeeks.getValue(subject).getValue(yearWeek)
    }
}
