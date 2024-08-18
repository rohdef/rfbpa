package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface WeekSynchronizationRepository {
    fun markForSynchronization(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<CannotChangeSyncronizationState, Unit>
    fun markPossiblyOutOfDate(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<CannotChangeSyncronizationState, Unit>

    fun markSynchronized(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<CannotChangeSyncronizationState, Unit>

    fun synchronizationStates(subject: RfbpaPrincipal.Subject, yearWeekInterval: YearWeekInterval): Map<YearWeek, SynchronizationState>
    fun synchronizationState(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): SynchronizationState

    enum class SynchronizationState {
        SYNCHRONIZED,
        POSSIBLY_OUT_OF_DATE,
        OUT_OF_DATE,
    }

    data class CannotChangeSyncronizationState(val subject: RfbpaPrincipal.Subject, val yearWeek: YearWeek)
}
