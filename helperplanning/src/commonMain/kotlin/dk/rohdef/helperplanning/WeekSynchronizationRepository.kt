package dk.rohdef.helperplanning

import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface WeekSynchronizationRepository {
    fun markForSynchronization(yearWeek: YearWeek)

    fun markSynchronized(yearWeek: YearWeek)
    fun markSynchronized(yearWeeks: List<YearWeek>)

    fun weeksToSynchronize(): List<YearWeek>
    fun weeksToSynchronize(yearWeekInterval: YearWeekInterval): List<YearWeek>
}
