package dk.rohdef.helperplanning

import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    internal val synchronizedWeeks = mutableSetOf<YearWeek>()

    fun reset() {
        synchronizedWeeks.clear()
    }

    override fun markForSynchronization(yearWeek: YearWeek) {
        synchronizedWeeks.remove(yearWeek)
    }

    override fun markSynchronized(yearWeek: YearWeek) {
        synchronizedWeeks.add(yearWeek)
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
