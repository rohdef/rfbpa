package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.datetime.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(YearWeek.YearWeekSearlizer::class)
data class YearWeek(
    val year: Int,
    val week: Int,
) : Comparable<YearWeek> {
    val firstDayOfWeek: LocalDate
        get() {
            return mondaysInWeeksOfYear(year).get(week - 1)
        }
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

    operator fun rangeTo(other: YearWeek): YearWeekInterval {
        if (this > other) {
            throw IllegalArgumentException("Other [$other] must be later than or equal to current [$this]")
        }

        return YearWeekInterval(this, other)
    }

    override fun compareTo(other: YearWeek): Int {
        val yearComparison = year.compareTo(other.year)

        if (yearComparison == 0) {
            return week.compareTo(other.week)
        }

        return yearComparison
    }

    override fun toString(): String {
        return "${year}-W${week.toString().padStart(2, '0')}"
    }

    /**
     * Gives the next year/week combination.
     *
     * This does not implement `operator fun inc()` because that motivates (forces if used) mutability.
     */
    fun nextWeek(): YearWeek {
        return if (this.week >= maxWeekForYear) {
            YearWeek(year + 1, 1)
        } else {
            YearWeek(year, week + 1)
        }
    }

    fun previousWeek(): YearWeek {
        return if (this.week == 1) {
            YearWeek(year - 1, mondaysInWeeksOfYear(year - 1).size)
        } else {
            YearWeek(year, week - 1)
        }
    }

    fun weeksUntil(other: YearWeek): Int {
        return (firstDayOfWeek.daysUntil(other.firstDayOfWeek) / 7)
    }

    fun atDayOfWeek(dayOfWeek: DayOfWeek) = YearWeekDay(this, dayOfWeek)

    companion object {
        fun parse(text: String): Either<YearWeekParseError, YearWeek> = either {
            ensure(text.length >= 7 && text.length <= 8) { YearWeekParseError.ContentsLenghtIsWrong(text, text.length) }

            val yearPart = text.substring(0, 4)

            val wAndWeekPart = if (text[4] == '-') {
                text.substring(5)
            } else {
                text.substring(4)
            }

            val year = catch({ yearPart.toInt() }) {
                raise(YearWeekParseError.YearMustBeANumber(yearPart, text))
            }

            val prefixRegex = "^([^0-9]*).*".toRegex()
            val weekPrefix = prefixRegex.find(wAndWeekPart)!!.groupValues[1]
            ensure(weekPrefix == "W") {
                YearWeekParseError.WeekMustBePrefixedWithW(weekPrefix, text)
            }

            val weekPart = wAndWeekPart.substring(1)
            ensure(weekPart.length == 2) { YearWeekParseError.WeekNumberMustBeTwoDigits(weekPart, text) }

            val week = catch({ weekPart.toInt() }) {
                raise(YearWeekParseError.WeekMustBeANumber(weekPart, text))
            }

            YearWeek(year, week)
        }

        private val mondaysInWeeksOfYear = LazyMap<Int, List<LocalDate>> {
            val firstDayOfYear = LocalDate.parse("${it - 1}-12-29")
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
    ) : IndexOutOfBoundsException("Invalid week number given [$weekGiven] for year [$yearGiven].  Valid weeks are [1-$maximumValidWeekNumber].") {
        /**
         * This cannot be null barring a faulty implementation of IndexOutOfBoundsException
         */
        override val message = super.message!!
    }

    object YearWeekSearlizer : KSerializer<YearWeek> {
        override val descriptor = PrimitiveSerialDescriptor("YearWeek", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: YearWeek) {
            val string = "${value.year}-W${value.week}"
            encoder.encodeString(string)
        }

        override fun deserialize(decoder: Decoder): YearWeek {
            val text = decoder.decodeString()
            val yearWeek = parse(text)

            when (yearWeek) {
                is Either.Right -> return yearWeek.value
                is Either.Left -> throw IllegalArgumentException("Could not deserialize year and week: ${yearWeek.value}")
            }
        }
    }
}
