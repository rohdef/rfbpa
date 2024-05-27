package dk.rohdef.rfweeks

import kotlinx.datetime.*

data class YearWeek(
    val year: Int,
    val week: Int,
) : Comparable<YearWeek> {
    val firstDayOfWeek: LocalDate
        get() { return mondaysInWeeksOfYear(year).get(week-1) }
    private val maxWeekForYear = mondaysInWeeksOfYear(year).size

    init {
        if (week <= 0) {
            throw InvalidWeekOfYear(
                year,
                week,
                maxWeekForYear,
            )
        }
        if (week > maxWeekForYear) {
            throw InvalidWeekOfYear(
                year,
                week,
                maxWeekForYear,
            )
        }
    }

    operator fun rangeTo(other: YearWeek): YearWeekRange {
        if (this > other) {
            throw IllegalArgumentException("Other must be later than current")
        }

        return YearWeekRange(this, other)
    }

    override fun compareTo(other: YearWeek): Int {
        val yearComparison = year.compareTo(other.year)

        if (yearComparison == 0) {
            return week.compareTo(other.week)
        }

        return yearComparison
    }

    /**
     * Gives the next year/week combination.
     *
     * This does not implement `operator fun inc()` because that motivates (forces if used) mutability.
     */
    fun increment(): YearWeek {
        return if (this.week >= maxWeekForYear) {
            YearWeek(year + 1, 1)
        } else {
            YearWeek(year, week + 1)
        }
    }

    companion object {
        private val mondaysInWeeksOfYear = LazyMap<Int, List<LocalDate>> {
            val firstDayOfYear = LocalDate.parse("${it-1}-12-29")
            val firstDayOfNextYear = LocalDate.parse("$it-12-29")
            val firstMondayOfYear = firstDayOfYear.nextOrSame(DayOfWeek.MONDAY)

            val allMondaysInYear = generateSequence(firstMondayOfYear) {
                val next = it + DatePeriod(days = 7)
                if (next >= firstDayOfNextYear) {
                    null
                } else {
                    next
                }
            }

            allMondaysInYear.toList()
        }

        /**
         * This follows the Danish definition that a week starts on Mondays
         *
         * Due to the ISO8601 week 1 can start in the year before.
         * In this implementation week 1 is always part of the year that has the closest January the 1st.
         *
         * Rules are:
         * the first Monday on or after December the 29th marks week 1.
         * if week 1 in year Y starts on December the 29th, 30th or 31st then that week 1 is in year Y+1.
         *
         * Examples:
         * - In 2020 December the 28th is a Monday.
         * Since this is before December the 29th - week 1 of 2021 starts on January the 4th 2021
         *
         * - In 2013 December the 29th is a Monday
         * Since this is on December the 29th - week 1 of 2014 starts on December the 29th 2013
         */
        fun mondaysInWeeksOfYear(year: Int): List<LocalDate> =
            mondaysInWeeksOfYear.get(year)

        private fun LocalDate.nextOrSame(dayOfWeek: DayOfWeek): LocalDate {
            if (this.dayOfWeek == dayOfWeek) {
                return this
            }

            val daysToAdd = 7 - (this.dayOfWeek.value - dayOfWeek.value)
            return this + DatePeriod(days = daysToAdd)
        }
    }


    class InvalidWeekOfYear(
        val yearGiven: Int,
        val weekGiven: Int,
        val maximumValidWeekNumber: Int,
    ): IndexOutOfBoundsException("Invalid week number given [$weekGiven] for year [$yearGiven].  Valid weeks are [1-$maximumValidWeekNumber].") {
        /**
         * This cannot be null barring a faulty implementation of IndexOutOfBoundsException
         */
        override val message = super.message!!
    }
}
