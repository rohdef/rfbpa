package dk.rohdef.rfweeks

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import java.time.DayOfWeek

class YearWeekDayTest : FunSpec({
    //        DayOfWeek.MONDAY
//
//        val input = listOf(
//            "2024-W12",
//            "2024-W12-3",
//            "2023W04",
//            "2023W045",
//        )
//        val inputWithTime = listOf(
//            "2024-W12-3T11:53",
//            "2023W045T09:18",
//        )
//        val inputRelaxed = listOf(
//            "2023-w02",
//            "2023-w02-1",
//            "2022w01",
//            "2022w012",
//        )
//        val badInput = listOf(
//            "24-W11",
//            "2024-W11-0",
//            "2024-W11-8",
//            "2024-W1",
//            "2024-W1-3",
//        )
    xcontext("Comparisons") {}

    xcontext("Parsing") {
    }

    context("from other types") {
        context("LocalDate") {
            test("a Monday") {
                val date = LocalDate(2024, 6, 3)

                YearWeekDay.from(date) shouldBe YearWeekDay(2024, 23, DayOfWeek.MONDAY)
            }

            test("a Thursday") {
                val date = LocalDate(2024, 6, 6)

                YearWeekDay.from(date) shouldBe YearWeekDay(2024, 23, DayOfWeek.THURSDAY)
            }
        }
    }

    context("to other types") {
        context("LocalDate") {
            // TODO test date val
        }
    }
})
