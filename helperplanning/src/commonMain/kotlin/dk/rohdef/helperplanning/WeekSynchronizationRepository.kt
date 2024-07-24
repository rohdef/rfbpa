package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface WeekSynchronizationRepository {
    fun markForSynchronization(yearWeek: YearWeek): Either<CannotChangeSyncronizationState, Unit>

    fun markSynchronized(yearWeek: YearWeek): Either<CannotChangeSyncronizationState, Unit>

    fun synchronizationStates(yearWeekInterval: YearWeekInterval): Map<YearWeek, SynchronizationState>
    fun synchronizationState(yearWeek: YearWeek): SynchronizationState

    enum class SynchronizationState {
        SYNCHRONIZED,
        OUT_OF_DATE,
    }

    data class CannotChangeSyncronizationState(val yearWeek: YearWeek)
}
