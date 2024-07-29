package dk.rohdef.rfweeks

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class YearWeekTest : FunSpec({
    context("Constructor") {
        test("Week number 0") {
            val exception = shouldThrow<YearWeek.InvalidWeekOfYear> {
                YearWeek(2018, 0)
            }

            exception.weekGiven shouldBeEqual 0
            exception.yearGiven shouldBeEqual 2018
            exception.maximumValidWeekNumber shouldBeEqual 52
            exception.message shouldBeEqual "Invalid week number given [0] for year [2018].  Valid weeks are [1-52]."
        }

        test("Week number -1") {
            val exception = shouldThrow<YearWeek.InvalidWeekOfYear> {
                YearWeek(2014, -1)
            }

            exception.weekGiven shouldBeEqual -1
            exception.yearGiven shouldBeEqual 2014
            exception.maximumValidWeekNumber shouldBeEqual 52
            exception.message shouldBeEqual "Invalid week number given [-1] for year [2014].  Valid weeks are [1-52]."
        }

        test("Week number 53 in year with 53 weeks") {
            YearWeek(2015, 53)
        }

        test("Week number 52 in year with 52 weeks") {
            YearWeek(2016, 52)
        }

        test("Week number 53 in year with 52 weeks") {
            val exception = shouldThrow<YearWeek.InvalidWeekOfYear> {
                YearWeek(2014, 53)
            }

            exception.weekGiven shouldBeEqual 53
            exception.yearGiven shouldBeEqual 2014
            exception.maximumValidWeekNumber shouldBeEqual 52
            exception.message shouldBeEqual "Invalid week number given [53] for year [2014].  Valid weeks are [1-52]."
        }

        test("Week number 54 in year with 53 weeks") {
            val exception = shouldThrow<YearWeek.InvalidWeekOfYear> {
                YearWeek(2015, 54)
            }

            exception.weekGiven shouldBeEqual 54
            exception.yearGiven shouldBeEqual 2015
            exception.maximumValidWeekNumber shouldBeEqual 53
            exception.message shouldBeEqual "Invalid week number given [54] for year [2015].  Valid weeks are [1-53]."
        }
    }

    context("Comparisons") {
        test("less than") {
            val basePoint = YearWeek(2023, 19)
            val justBefore = YearWeek(2023, 18)
            val yearBefore = YearWeek(2022, 19)
            val longBefore = YearWeek(2005, 21)

            basePoint shouldBeGreaterThan justBefore
            basePoint shouldBeGreaterThan yearBefore
            basePoint shouldBeGreaterThan longBefore
        }

        test("equal to") {
            val first = YearWeek(2023, 19)
            val second = YearWeek(1990, 3)
            val thirds = YearWeek(2035, 34)

            first shouldBeEqual YearWeek(2023, 19)
            second shouldBeEqual YearWeek(1990, 3)
            thirds shouldBeEqual YearWeek(2035, 34)
        }

        test("greater than") {
            val basePoint = YearWeek(2023, 19)
            val justAfter = YearWeek(2023, 20)
            val yearAfter = YearWeek(2024, 19)
            val longAfter = YearWeek(2035, 8)

            basePoint shouldBeLessThan justAfter
            basePoint shouldBeLessThan yearAfter
            basePoint shouldBeLessThan longAfter
        }
    }

    context("Mondays in a year") {
        test("2015") {
            val year = 2015
            val mondaysIn2015 = listOf(
                LocalDate.parse("${year-1}-12-29"),
                LocalDate.parse("$year-01-05"),
                LocalDate.parse("$year-01-12"),
                LocalDate.parse("$year-01-19"),
                LocalDate.parse("$year-01-26"),
                LocalDate.parse("$year-02-02"),
                LocalDate.parse("$year-02-09"),
                LocalDate.parse("$year-02-16"),
                LocalDate.parse("$year-02-23"),
                LocalDate.parse("$year-03-02"),
                LocalDate.parse("$year-03-09"),
                LocalDate.parse("$year-03-16"),
                LocalDate.parse("$year-03-23"),
                LocalDate.parse("$year-03-30"),
                LocalDate.parse("$year-04-06"),
                LocalDate.parse("$year-04-13"),
                LocalDate.parse("$year-04-20"),
                LocalDate.parse("$year-04-27"),
                LocalDate.parse("$year-05-04"),
                LocalDate.parse("$year-05-11"),
                LocalDate.parse("$year-05-18"),
                LocalDate.parse("$year-05-25"),
                LocalDate.parse("$year-06-01"),
                LocalDate.parse("$year-06-08"),
                LocalDate.parse("$year-06-15"),
                LocalDate.parse("$year-06-22"),
                LocalDate.parse("$year-06-29"),
                LocalDate.parse("$year-07-06"),
                LocalDate.parse("$year-07-13"),
                LocalDate.parse("$year-07-20"),
                LocalDate.parse("$year-07-27"),
                LocalDate.parse("$year-08-03"),
                LocalDate.parse("$year-08-10"),
                LocalDate.parse("$year-08-17"),
                LocalDate.parse("$year-08-24"),
                LocalDate.parse("$year-08-31"),
                LocalDate.parse("$year-09-07"),
                LocalDate.parse("$year-09-14"),
                LocalDate.parse("$year-09-21"),
                LocalDate.parse("$year-09-28"),
                LocalDate.parse("$year-10-05"),
                LocalDate.parse("$year-10-12"),
                LocalDate.parse("$year-10-19"),
                LocalDate.parse("$year-10-26"),
                LocalDate.parse("$year-11-02"),
                LocalDate.parse("$year-11-09"),
                LocalDate.parse("$year-11-16"),
                LocalDate.parse("$year-11-23"),
                LocalDate.parse("$year-11-30"),
                LocalDate.parse("$year-12-07"),
                LocalDate.parse("$year-12-14"),
                LocalDate.parse("$year-12-21"),
                LocalDate.parse("$year-12-28"),
            )

            val actualMondaysIn2015 = YearWeek.mondaysInWeeksOfYear(year)

            actualMondaysIn2015 shouldContainExactly mondaysIn2015
        }

        test("2019") {
            val year = 2019
            val mondaysIn2019 = listOf(
                LocalDate.parse("${year-1}-12-31"),
                LocalDate.parse("$year-01-07"),
                LocalDate.parse("$year-01-14"),
                LocalDate.parse("$year-01-21"),
                LocalDate.parse("$year-01-28"),
                LocalDate.parse("$year-02-04"),
                LocalDate.parse("$year-02-11"),
                LocalDate.parse("$year-02-18"),
                LocalDate.parse("$year-02-25"),
                LocalDate.parse("$year-03-04"),
                LocalDate.parse("$year-03-11"),
                LocalDate.parse("$year-03-18"),
                LocalDate.parse("$year-03-25"),
                LocalDate.parse("$year-04-01"),
                LocalDate.parse("$year-04-08"),
                LocalDate.parse("$year-04-15"),
                LocalDate.parse("$year-04-22"),
                LocalDate.parse("$year-04-29"),
                LocalDate.parse("$year-05-06"),
                LocalDate.parse("$year-05-13"),
                LocalDate.parse("$year-05-20"),
                LocalDate.parse("$year-05-27"),
                LocalDate.parse("$year-06-03"),
                LocalDate.parse("$year-06-10"),
                LocalDate.parse("$year-06-17"),
                LocalDate.parse("$year-06-24"),
                LocalDate.parse("$year-07-01"),
                LocalDate.parse("$year-07-08"),
                LocalDate.parse("$year-07-15"),
                LocalDate.parse("$year-07-22"),
                LocalDate.parse("$year-07-29"),
                LocalDate.parse("$year-08-05"),
                LocalDate.parse("$year-08-12"),
                LocalDate.parse("$year-08-19"),
                LocalDate.parse("$year-08-26"),
                LocalDate.parse("$year-09-02"),
                LocalDate.parse("$year-09-09"),
                LocalDate.parse("$year-09-16"),
                LocalDate.parse("$year-09-23"),
                LocalDate.parse("$year-09-30"),
                LocalDate.parse("$year-10-07"),
                LocalDate.parse("$year-10-14"),
                LocalDate.parse("$year-10-21"),
                LocalDate.parse("$year-10-28"),
                LocalDate.parse("$year-11-04"),
                LocalDate.parse("$year-11-11"),
                LocalDate.parse("$year-11-18"),
                LocalDate.parse("$year-11-25"),
                LocalDate.parse("$year-12-02"),
                LocalDate.parse("$year-12-09"),
                LocalDate.parse("$year-12-16"),
                LocalDate.parse("$year-12-23"),
            )

            val actualMondaysIn2019 = YearWeek.mondaysInWeeksOfYear(year)

            actualMondaysIn2019 shouldContainExactly mondaysIn2019
        }
    }

    context("Incrementor") {
        test("Week in the middle of the year") {
            val yearWeek = YearWeek(2023, 6)

            val nextYearWeek = yearWeek.nextWeek()

            nextYearWeek shouldBe YearWeek(2023, 7)
        }

        test("Last week in year with 52 weeks") {
            val yearWeek = YearWeek(2014, 52)

            val nextYearWeek = yearWeek.nextWeek()

            nextYearWeek shouldBe YearWeek(2015, 1)
        }

        test("Week 52 in year with 53 weeks") {
            val yearWeek = YearWeek(2015, 52)

            val nextYearWeek = yearWeek.nextWeek()

            nextYearWeek shouldBe YearWeek(2015, 53)
        }

        test("Last week in year with 53 weeks") {
            val yearWeek = YearWeek(2020, 53)

            val nextYearWeek = yearWeek.nextWeek()

            nextYearWeek shouldBe YearWeek(2021, 1)
        }
    }

    test("range to") {
        val yearWeekInterval = YearWeek(2023, 16)..YearWeek(2023, 19)

        yearWeekInterval.start shouldBe YearWeek(2023, 16)
        yearWeekInterval.endInclusive shouldBe YearWeek(2023, 19)
    }

    context("Parsing") {
        test("full notation") {
            val text = "2024-W12"
            val yearWeek = YearWeek.parse(text)
            yearWeek shouldBeRight YearWeek(2024, 12)
        }

        test("short notation") {
            val text = "2022W08"
            val yearWeek = YearWeek.parse(text)
            yearWeek shouldBeRight YearWeek(2022, 8)
        }

        test("year must be a number") {
            val text = "2x22-W08"
            val yearWeek = YearWeek.parse(text)
            yearWeek shouldBeLeft YearWeekParseError.YearMustBeANumber(
                "2x22",
                text,
            )
        }

        test("week part must be prefixed with W") {
            val noPrefix = "2022-08"
            val yearWeekNoPrefix = YearWeek.parse(noPrefix)
            yearWeekNoPrefix shouldBeLeft YearWeekParseError.WeekMustBePrefixedWithW(
                "",
                noPrefix,
            )

            val wrongPrefix = "2022-T08"
            val yearWeekWrongPrefix = YearWeek.parse(wrongPrefix)
            yearWeekWrongPrefix shouldBeLeft YearWeekParseError.WeekMustBePrefixedWithW(
                "T",
                wrongPrefix,
            )

            val longWrongPrefix = "2022-WT08"
            val yearWeekLongWrongPrefix = YearWeek.parse(longWrongPrefix)
            yearWeekLongWrongPrefix shouldBeLeft YearWeekParseError.WeekMustBePrefixedWithW(
                "WT",
                longWrongPrefix,
            )
        }

        test("Week must be a number") {
            val text = "2024-W3F"
            val yearWeek = YearWeek.parse(text)
            yearWeek shouldBeLeft YearWeekParseError.WeekMustBeANumber(
                "3F",
                text,
            )
        }

        xtest("Week must be two digits") {
            val text = "2022W8"
            val yearWeek = YearWeek.parse(text)
            yearWeek shouldBeLeft YearWeekParseError.WeekNumberMustBeTwoDigits(
                "8",
                text
            )
        }
    }

    context("Formatting as string") {
        test("uses hyphen separator") {
            val yearWeekInterval = YearWeek(1812, 6)

            yearWeekInterval.toString() shouldBe "1812-W06"
        }
    }

    test("at day of week") {
        val yearWeek = YearWeek(1992, 7)
        val yearWeekDay = yearWeek.atDayOfWeek(DayOfWeek.SUNDAY)

        yearWeekDay.year shouldBe 1992
        yearWeekDay.week shouldBe 7
        yearWeekDay.dayOfWeek shouldBe DayOfWeek.SUNDAY
    }
})
