package dk.rohdef.helperplanning

import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface WeekSynchronizationRepository {
    fun markForSynchronization(yearWeek: YearWeek)

    fun markSynchronized(yearWeek: YearWeek)
    fun markSynchronized(yearWeeks: List<YearWeek>)

    fun synchronizationStates(yearWeekInterval: YearWeekInterval): Map<YearWeek, SynchronizationState>
    fun synchronizationState(yearWeek: YearWeek): SynchronizationState

    enum class SynchronizationState {
        SYNCHRONIZED,
        OUT_OF_DATE,
    }
}
