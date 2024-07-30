package dk.rohdef.rfweeks

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek

class YearWeekDayAtTimeTest : FunSpec({
    context("Parsing") {
        test("should parse") {
            val text = "2018-W02-6T11:53"
            val parsed = YearWeekDayAtTime.parse(text)

            val yearWeekDayAtTime = parsed.shouldBeRight()
            yearWeekDayAtTime.year shouldBe 2018
            yearWeekDayAtTime.week shouldBe 2
            yearWeekDayAtTime.dayOfWeek shouldBe DayOfWeek.SATURDAY
            yearWeekDayAtTime.time.hour shouldBe 11
            yearWeekDayAtTime.time.minute shouldBe 53
        }
    }

    test("Formatting") {
        val text1 = "1914-W21-1T04:11"
        val parsed1 = YearWeekDayAtTime.parse(text1).shouldBeRight()

        val toString1 = parsed1.toString()

        toString1 shouldBe text1

        val text2 = "2018-W02-6T11:53"
        val parsed2 = YearWeekDayAtTime.parse(text2).shouldBeRight()

        val toString2 = parsed2.toString()

        toString2 shouldBe text2
    }

    context("Serialization") {
        test("Should serialize to ISO-8601") {
            val yearWeekDayAtTime = YearWeekDayAtTime(
                YearWeekDay(1992, 7, DayOfWeek.SUNDAY),
                LocalTime(12, 41),
            )

            val encodedDay = Json.encodeToString(yearWeekDayAtTime)

            encodedDay shouldBe "\"1992-W07-7T12:41\""
        }

        test("Should deserialize from ISO-8601") {
            val text = "\"1993-W30-6T08:11\""

            val decoded = Json.decodeFromString<YearWeekDayAtTime>(text)

            decoded shouldBe YearWeekDayAtTime(
                YearWeekDay(1993, 30, DayOfWeek.SATURDAY),
                LocalTime(8, 11),
            )
        }

        test("Should fail if not ISO-8601") {
            val text = "\"1993-W30-6\""

            shouldThrow<IllegalArgumentException> { Json.decodeFromString<YearWeekDayAtTime>(text) }
        }
    }
})
