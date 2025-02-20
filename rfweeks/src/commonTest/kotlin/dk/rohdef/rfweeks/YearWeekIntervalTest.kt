package dk.rohdef.rfweeks

import arrow.core.nonEmptyListOf
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

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

        test("using hyphen seperator") {
            val text = "2024-W19--2024-W41"
            val parsed = YearWeekInterval.parse(text)

            val yearWeekInterval = parsed.shouldBeRight()
            yearWeekInterval.start shouldBe YearWeek(2024, 19)
            yearWeekInterval.endInclusive shouldBe YearWeek(2024, 41)
        }
    }

    context("Formatting as string") {
        test("uses hyphen separator") {
            val yearWeekInterval = YearWeek(2024, 1)..YearWeek(2024, 5)

            yearWeekInterval.toString() shouldBe "2024-W01--2024-W05"
        }
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

            context("Invalid separators") {
                test("separator not found") {
                    val text = "week8-to-week9"

                    val parsed = YearWeekInterval.parse(text).shouldBeLeft()

                    parsed shouldBe nonEmptyListOf(
                        YearWeekIntervalParseError.NoSeparatorError(text)
                    )
                }

                xtest("too many separators found") {}

                xtest("mixed usage of separators (too many separators variation)") {}
            }
        }
    }

    context("serialization") {
        context("encoding") {
            test("not implemented") {
                shouldThrow<UnsupportedOperationException> {
                    Json.encodeToString(
                        YearWeekInterval.serializer(),
                        YearWeek(2024, 4)..YearWeek(2024, 5),
                    )
                }
            }
        }

        context("decoding") {
            test("domain exception with parse error") {
                val pattern = "2000-W032000-W04"
                val exception = shouldThrow<YearWeekIntervalParseException> {
                    Json.decodeFromString(
                        YearWeekInterval.serializer(),
                        "\"$pattern\"",
                    )
                }

                exception.errors shouldContainExactlyInAnyOrder listOf(
                    YearWeekIntervalParseError.NoSeparatorError(pattern),
                )
            }

            test("domain exception with multiple parse errors") {
                val pattern = "20x0-W03--2000-W4"
                val exception = shouldThrow<YearWeekIntervalParseException> {
                    Json.decodeFromString(
                        YearWeekInterval.serializer(),
                        "\"$pattern\"",
                    )
                }

                exception.errors shouldContainExactlyInAnyOrder listOf(
                    YearWeekIntervalParseError.YearWeekComponentParseError(
                        pattern,
                        YearWeekIntervalParseError.IntervalPart.START,
                        YearWeekParseError.YearMustBeANumber("20x0", "20x0-W03"),
                    ),
                    YearWeekIntervalParseError.YearWeekComponentParseError(
                        pattern,
                        YearWeekIntervalParseError.IntervalPart.END,
                        YearWeekParseError.WeekNumberMustBeTwoDigits("4", "2000-W4"),
                    ),
                )
            }
        }
    }
})
