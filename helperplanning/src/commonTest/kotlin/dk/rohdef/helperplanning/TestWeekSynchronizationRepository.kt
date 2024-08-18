package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.rfweeks.YearWeek

typealias MarkSynchronizedErrorRunner = (yearWeek: YearWeek) -> Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit>

class TestWeekSynchronizationRepository(
    val memoryWeekSynchronizationRepository: MemoryWeekSynchronizationRepository = MemoryWeekSynchronizationRepository(),
) : WeekSynchronizationRepository by memoryWeekSynchronizationRepository {
    private val _markSynchronizedPreRunners = mutableListOf<MarkSynchronizedErrorRunner>()
    fun addMarkSynchronizedPreRunners(markSynchronizer: MarkSynchronizedErrorRunner) {
        _markSynchronizedPreRunners.add(markSynchronizer)
    }

    fun reset() {
        _markSynchronizedPreRunners.clear()
        memoryWeekSynchronizationRepository.reset()
    }

    override fun markSynchronized(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<WeekSynchronizationRepository.CannotChangeSyncronizationState, Unit> = either {
        _markSynchronizedPreRunners.map { it(yearWeek).bind() }
        memoryWeekSynchronizationRepository.markSynchronized(subject, yearWeek)
    }
}
