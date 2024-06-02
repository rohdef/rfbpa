package dk.rohdef.rfweeks

import arrow.core.nonEmptyListOf
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class YearWeekIntervalTest : FunSpec({
    context("Year week interval parser - ISO8601") {
        test("using long specification") {
            val text = "2024-W22/2024-W24"
            val parsed = YearWeekInterval.parse(text)

            val yearWeekInterval = parsed.shouldBeRight()
            yearWeekInterval.start shouldBe YearWeek(2024, 22)
            yearWeekInterval.endInclusive shouldBe YearWeek(2024, 24)
        }

        test("using short specification") {
            val text = "2011W03/2019W09"
            val parsed = YearWeekInterval.parse(text)

            val yearWeekInterval = parsed.shouldBeRight()
            yearWeekInterval.start shouldBe YearWeek(2011, 3)
            yearWeekInterval.endInclusive shouldBe YearWeek(2019, 9)
        }
    }

    context("Invalid separators") {
        xtest("separator not found") {}

        xtest("too many separators found") {}

        xtest("mixed usage of separators (too many separators variation)") {}
    }

    xtest("start must be before end") {}

    context("invalid component specifications") {
        context("start and end doesn't follow a year week specification") {
            test("wrong first") {
                val first = "2x04-W13"
                val second = "2022-W01"
                val text = "${first}/${second}"
                val parsed = YearWeekInterval.parse(text)
                parsed shouldBeLeft nonEmptyListOf(
                    YearWeekIntervalParseError.YearWeekComponentParseError(
                        text,
                        YearWeekIntervalParseError.IntervalPart.START,
                        YearWeekParseError.YearMustBeANumber(
                            "2x04",
                            first,
                        ),
                    ),
                )
            }

            test("wrong last") {
                val first = "2017-W43"
                val second = "2018-X33"
                val text = "${first}/${second}"
                val parsed = YearWeekInterval.parse(text)
                parsed shouldBeLeft nonEmptyListOf(
                    YearWeekIntervalParseError.YearWeekComponentParseError(
                        text,
                        YearWeekIntervalParseError.IntervalPart.END,
                        YearWeekParseError.WeekMustBePrefixedWithW(
                            "X",
                            second,
                        ),
                    ),
                )
            }

            test("both wrong") {
                val first = "1912-W4E"
                val second = "1915-X21"
                val text = "${first}/${second}"
                val parsed = YearWeekInterval.parse(text)
                parsed shouldBeLeft nonEmptyListOf(
                    YearWeekIntervalParseError.YearWeekComponentParseError(
                        text,
                        YearWeekIntervalParseError.IntervalPart.START,
                        YearWeekParseError.WeekMustBeANumber(
                            "4E",
                            first,
                        ),
                    ),
                    YearWeekIntervalParseError.YearWeekComponentParseError(
                        text,
                        YearWeekIntervalParseError.IntervalPart.END,
                        YearWeekParseError.WeekMustBePrefixedWithW(
                            "X",
                            second,
                        ),
                    ),
                )
            }
        }
    }
})
