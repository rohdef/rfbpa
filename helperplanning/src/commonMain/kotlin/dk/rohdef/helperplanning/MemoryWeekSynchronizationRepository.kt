package dk.rohdef.helperplanning

import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    val synchronizedWeeks = mutableSetOf<YearWeek>()

    override fun markForSynchronization(yearWeek: YearWeek) {
        synchronizedWeeks.remove(yearWeek)
    }

    override fun markSynchronized(yearWeek: YearWeek) {
        synchronizedWeeks.add(yearWeek)
    }

    override fun markSynchronized(yearWeeks: List<YearWeek>) {
        yearWeeks.forEach { markForSynchronization(it) }
    }

    override fun weeksToSynchronize(yearWeekInterval: YearWeekInterval): List<YearWeek> {
        return yearWeekInterval.toList()
            .filter { !synchronizedWeeks.contains(it) }
            .sorted()
    }
}
