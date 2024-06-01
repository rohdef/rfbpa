package dk.rohdef.rfweeks

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec

class YearWeekFormatTest : FunSpec({
    context("Format detection") {
        context("ISO8601") {
            test("long format") {
                val text = "2018-W52"
                val format = YearWeekFormat.formatMatch(text)
                format shouldBeRight YearWeekFormat.ISO_8601_LONG
            }

            test("short format") {
                val text = "2011W09"
                val format = YearWeekFormat.formatMatch(text)
                format shouldBeRight YearWeekFormat.ISO_8601_SHORT
            }
        }

        test("unknown format") {
            val text = "W11"
            val format = YearWeekFormat.formatMatch(text)
            format shouldBeLeft YearWeekFormat.FormatMatchError.NoFormatMatches(text)
        }
    }
})
