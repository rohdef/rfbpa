package dk.rohdef.helperplanning

import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    val weeksForSynchronization = mutableSetOf<YearWeek>()

    override fun markForSynchronization(yearWeek: YearWeek) {
        weeksForSynchronization.add(yearWeek)
    }

    override fun markSynchronized(yearWeek: YearWeek) {
        TODO("not implemented")
    }

    override fun markSynchronized(yearWeeks: List<YearWeek>) {
        TODO("not implemented")
    }

    override fun weeksToSynchronize(): List<YearWeek> =
        weeksForSynchronization.toList().sorted()

    override fun weeksToSynchronize(yearWeekInterval: YearWeekInterval): List<YearWeek> {
        TODO("not implemented")
    }
}
