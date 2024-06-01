package dk.rohdef.rfweeks

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

    context("invalid specifications") {
        test("start and end doesn't follow thhe same specification") {
            val shortFirstLongLast = ""
            val parsedShortFirst = YearWeekInterval.parse(shortFirstLongLast)
            parsedShortFirst shouldBeLeft TODO()

            val longFirstShortLast = ""
            val parsedLongFirst = YearWeekInterval.parse(longFirstShortLast)
            parsedLongFirst shouldBeRight TODO()
        }

        context("duration end doesn't follow specs") {
        }

        context("start isn't right") {
        }
    }
})
