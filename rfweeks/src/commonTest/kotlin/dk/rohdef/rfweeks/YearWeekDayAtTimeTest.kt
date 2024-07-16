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
})
