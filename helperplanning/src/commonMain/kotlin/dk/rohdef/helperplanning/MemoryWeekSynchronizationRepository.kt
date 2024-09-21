package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    val defaultSynchronization = WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE
    internal val synchronizedWeeks = mutableMapOf<RfbpaPrincipal.Subject, Map<YearWeek, WeekSynchronizationRepository.SynchronizationState>>()
        .withDefault { emptyMap<YearWeek, WeekSynchronizationRepository.SynchronizationState>().withDefault { defaultSynchronization } }

    fun reset() {
        synchronizedWeeks.clear()
    }

    override fun markForSynchronization(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> = either {
        synchronizedWeeks.letValue(subject) {
            (it - yearWeek).withDefault { defaultSynchronization }
        }
    }

    override fun markPossiblyOutOfDate(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> = either {
        synchronizedWeeks.letValue(subject) {
            ((it - yearWeek) + (yearWeek to WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE))
                .withDefault { defaultSynchronization }
        }
    }

    override fun markSynchronized(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> = either {
        synchronizedWeeks.letValue(subject) {
            ((it - yearWeek) + (yearWeek to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED))
                .withDefault { defaultSynchronization }
        }
    }

    override fun synchronizationStates(subject: RfbpaPrincipal.Subject, yearWeekInterval: YearWeekInterval): Map<YearWeek, WeekSynchronizationRepository.SynchronizationState> {
        return yearWeekInterval.associate { it to synchronizationState(subject, it) }
    }

    override fun synchronizationState(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): WeekSynchronizationRepository.SynchronizationState {
        return synchronizedWeeks.getValue(subject).getValue(yearWeek)
    }
}
