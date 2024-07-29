package dk.rohdef.rfweeks

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
})
