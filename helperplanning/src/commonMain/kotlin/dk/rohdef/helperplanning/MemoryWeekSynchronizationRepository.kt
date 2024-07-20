package dk.rohdef.helperplanning

import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

class MemoryWeekSynchronizationRepository : WeekSynchronizationRepository {
    override fun markForSynchronization(yearWeek: YearWeek) {
    }

    override fun markSynchronized(yearWeek: YearWeek) {
        TODO("not implemented")
    }

    override fun markSynchronized(yearWeeks: List<YearWeek>) {
        TODO("not implemented")
    }

    override fun weeksToSynchronize(): List<YearWeek> {
        TODO("not implemented")
    }

    override fun weeksToSynchronize(yearWeekInterval: YearWeekInterval): List<YearWeek> {
        TODO("not implemented")
    }
}
