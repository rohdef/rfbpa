package dk.rohdef.rfweeks

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class YearWeekRangeTest : FunSpec({
    context("Year week range parser - ISO8601") {
        test("using long specification") {
            val text = "2024-W22/2024-W24"
            val parsed = YearWeekRange.parse(text)

            val yearWeekRange = parsed.shouldBeRight()
            yearWeekRange.start shouldBe YearWeek(2024, 22)
            yearWeekRange.endInclusive shouldBe YearWeek(2024, 24)
        }

        test("using short specification") {
            val text = "2011W03/2019W09"
            val parsed = YearWeekRange.parse(text)

            val yearWeekRange = parsed.shouldBeRight()
            yearWeekRange.start shouldBe YearWeek(2011, 3)
            yearWeekRange.endInclusive shouldBe YearWeek(2019, 9)
        }
    }

    context("invalid specifications") {
        test("Should fail if duration start doesn't follow specs") {

        }

        context("duration end doesn't follow specs") {
            context("start isn't right")
        }
    }
})
