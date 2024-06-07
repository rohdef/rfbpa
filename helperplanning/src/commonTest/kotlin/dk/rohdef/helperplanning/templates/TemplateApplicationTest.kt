package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.TestWeekPlanRepository
import dk.rohdef.helperplanning.shifts.ShiftType
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.toYearWeekDay
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.*

class TemplateApplicationTest : FunSpec({
    val timezone = TimeZone.of("Europe/Copenhagen")
    val weekPlanRepository = TestWeekPlanRepository()
    val templateApplication = TemplateApplication(weekPlanRepository)

    fun Instant.toYearWeekDay() : YearWeekDay {
        return this.toLocalDateTime(timezone)
            .date
            .toYearWeekDay()
    }

    fun TestWeekPlanRepository.firstShiftStart() {
        this.sortedByStartShifts
            .first()
            .start
            .toYearWeekDay()
    }

    fun TestWeekPlanRepository.lastShiftStart() {
        this.sortedByStartShifts
            .last()
            .start
            .toYearWeekDay()
    }

    context("Template cutting dates") {
        val schedulingStart = YearWeek(1919, 12)
        val schedulingEnd = YearWeek(1919, 15)

        context("start date") {
            test("template is before scheduling") {
                val template = Template(
                    YearWeek(1919, 3),
                    TemplateTestData.WeekTemplates.week,
                )

                templateApplication.applyTemplate(schedulingStart, schedulingEnd, template)

                weekPlanRepository.firstShiftStart() shouldBe YearWeekDay(schedulingStart, DayOfWeek.MONDAY)
                weekPlanRepository.lastShiftStart() shouldBe YearWeekDay(schedulingEnd, DayOfWeek.SUNDAY)
            }

            test("template in between scheduling") {
                val template = Template(
                    YearWeek(1919, 13),
                    TemplateTestData.WeekTemplates.week,
                )

                templateApplication.applyTemplate(schedulingStart, schedulingEnd, template)

                weekPlanRepository.firstShiftStart() shouldBe YearWeekDay(1919, 13, DayOfWeek.MONDAY)
                weekPlanRepository.lastShiftStart() shouldBe YearWeekDay(schedulingEnd, DayOfWeek.SUNDAY)
            }

            test("template is after scheduling") {
                val template = Template(
                    YearWeek(1919, 16),
                    TemplateTestData.WeekTemplates.week,
                )

                templateApplication.applyTemplate(schedulingStart, schedulingEnd, template)

                weekPlanRepository.shifts.shouldBeEmpty()
            }
        }

        // TODO don't test old logic, but move to newest when possible
        xcontext("switches to the correct template") {
            test("two templates, schduling start is after second start") {
                val template = Template(
                    YearWeek(1919, 3),
                    TemplateTestData.WeekTemplates.week,
                )
            }

            test("none applied if scheduling end is before only start") {
                val template = Template(
                    YearWeek(1919, 3),
                    TemplateTestData.WeekTemplates.week,
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
