package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    internal val synchronizedWeeks = mutableSetOf<YearWeek>()

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
        TODO("not implemented")
    }

    override fun markSynchronized(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks.add(yearWeek)
        return Unit.right()
    }

    override fun synchronizationStates(subject: RfbpaPrincipal.Subject, yearWeekInterval: YearWeekInterval): Map<YearWeek, WeekSynchronizationRepository.SynchronizationState> {
        return yearWeekInterval.associate { it to synchronizationState(subject, it) }
    }

    override fun synchronizationState(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): WeekSynchronizationRepository.SynchronizationState {
        return if (synchronizedWeeks.contains(yearWeek)) {
            WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        } else {
            WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE
        }
    }
}
