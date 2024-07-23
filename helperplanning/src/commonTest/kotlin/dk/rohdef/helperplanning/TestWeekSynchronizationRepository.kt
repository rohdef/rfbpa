package dk.rohdef.helperplanning

import dk.rohdef.rfweeks.YearWeek

typealias MarkSynchronizedPreRunner = (yearWeek: YearWeek) -> Unit

class TestWeekSynchronizationRepository(
    val memoryWeekSynchronizationRepository: MemoryWeekSynchronizationRepository = MemoryWeekSynchronizationRepository(),
) : WeekSynchronizationRepository by memoryWeekSynchronizationRepository {
    private val _markSynchronizedPreRunners = mutableListOf<MarkSynchronizedPreRunner>()
    fun addMarkSynchronizedPreRunner(preRunner: MarkSynchronizedPreRunner) {
        _markSynchronizedPreRunners.add(preRunner)
    }

    fun reset() {
        memoryWeekSynchronizationRepository.reset()
    }

    override fun markSynchronized(yearWeek: YearWeek) {
        _markSynchronizedPreRunners.forEach { it(yearWeek) }
        return memoryWeekSynchronizationRepository.markSynchronized(yearWeek)
    }
}
