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

    override fun markForSynchronization(yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks.remove(yearWeek)
            return Unit.right()
    }

    override fun markSynchronized(yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> {
        synchronizedWeeks.add(yearWeek)
        return Unit.right()
    }

    override fun synchronizationStates(yearWeekInterval: YearWeekInterval): Map<YearWeek, WeekSynchronizationRepository.SynchronizationState> {
        return yearWeekInterval.associate { it to synchronizationState(it) }
    }

    override fun synchronizationState(yearWeek: YearWeek): WeekSynchronizationRepository.SynchronizationState {
        return if (synchronizedWeeks.contains(yearWeek)) {
            WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        } else {
            WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE
        }
    }
}
