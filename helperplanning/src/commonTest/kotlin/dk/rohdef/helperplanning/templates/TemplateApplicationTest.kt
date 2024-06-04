package dk.rohdef.helperplanning.templates

import arrow.core.none
import dk.rohdef.helperplanning.TestWeekPlanRepository
import dk.rohdef.helperplanning.shifts.ShiftType
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.toYearWeekDay
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TemplateApplicationTest : FunSpec({
    val weekPlanRepository = TestWeekPlanRepository()
    val templateApplication = TemplateApplication(weekPlanRepository)

    context("Template cutting dates") {
        val schedulingStart = YearWeek(1919, 12)
        val schedulingEnd = YearWeek(1919, 15)

        val weekTemplates = listOf(
            WeekTemplate(
                "", mapOf(
                    DayOfWeek.FRIDAY to listOf(TemplateTestData.ShiftTemplates.shift6_30to22_30)
                )
            )
        )

        context("start date") {
            test("template is before scheduling") {
                val template = Template(
                    YearWeek(1919, 3),
                    weekTemplates,
                )

                templateApplication.performThisBitch(
                    schedulingStart,
                    schedulingEnd,
                    listOf(template),
                )

                // check first shift is monday of week 12
                val firstShift = weekPlanRepository.sortedByStartShifts.first()
                val lastShift = weekPlanRepository.sortedByStartShifts.last()
                val firstStart = firstShift.start
                    // TODO use application time zone
                    .toLocalDateTime(TimeZone.UTC)
                    .date.toYearWeekDay()
                firstStart shouldBe YearWeekDay(1919, 12, DayOfWeek.MONDAY)
                val lastStart = lastShift.start // note datecrossing shift is possible and valid
                    .toLocalDateTime(TimeZone.UTC)
                    .date.toYearWeekDay()
                lastStart shouldBe YearWeekDay(1919, 15, DayOfWeek.SUNDAY)
            }

            test("template is after scheduling") {
                val template = Template(
                    YearWeek(1919, 3),
                    weekTemplates,
                )
            }
        }

        // TODO don't test old logic, but move to newest when possible
        xcontext("switches to the correct template") {
            test("two templates, schduling start is after second start") {
                val template = Template(
                    YearWeek(1919, 3),
                    weekTemplates,
                )
            }

            test("none applied if scheduling end is before only start") {
                val template = Template(
                    YearWeek(1919, 3),
                    weekTemplates,
                )
            }

            test("scheduling touches two templates") {

            }
        }
    }

    context("context apply week templates") {
        val week = YearWeek(2024, 12)

        val template = WeekTemplate(
            "",
            mapOf(
                DayOfWeek.MONDAY to listOf(),
                DayOfWeek.TUESDAY to listOf(),
                DayOfWeek.WEDNESDAY to listOf(),
                DayOfWeek.THURSDAY to listOf(),
                DayOfWeek.FRIDAY to listOf(),
                DayOfWeek.SATURDAY to listOf(),
                DayOfWeek.SUNDAY to listOf(),
            ),
        )
    }

    context("week template dates") {}

    context("repeating week templates") {
        val template1 = WeekTemplate(
            "",
            mapOf(
                DayOfWeek.MONDAY to listOf(
                    ShiftTemplate(
                        HelperReservation.Helper(""),
                        ShiftType.DAY,
                        LocalTime(6, 15),
                        LocalTime(12, 45),
                    )
                ),
                DayOfWeek.TUESDAY to listOf(),
                DayOfWeek.WEDNESDAY to listOf(),
                DayOfWeek.THURSDAY to listOf(),
                DayOfWeek.FRIDAY to listOf(),
                DayOfWeek.SATURDAY to listOf(),
                DayOfWeek.SUNDAY to listOf(),
            ),
        )

        val template2 = WeekTemplate(
            "",
            mapOf(
                DayOfWeek.MONDAY to listOf(),
                DayOfWeek.TUESDAY to listOf(),
                DayOfWeek.WEDNESDAY to listOf(),
                DayOfWeek.THURSDAY to listOf(),
                DayOfWeek.FRIDAY to listOf(),
                DayOfWeek.SATURDAY to listOf(),
                DayOfWeek.SUNDAY to listOf(),
            ),
        )

        val template3 = WeekTemplate(
            "",
            mapOf(
                DayOfWeek.MONDAY to listOf(),
                DayOfWeek.TUESDAY to listOf(),
                DayOfWeek.WEDNESDAY to listOf(),
                DayOfWeek.THURSDAY to listOf(),
                DayOfWeek.FRIDAY to listOf(),
                DayOfWeek.SATURDAY to listOf(),
                DayOfWeek.SUNDAY to listOf(),
            ),
        )
    }
})
