package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.TestWeekPlanRepository
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftType
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class TemplateApplierTest : FunSpec({
    val weekPlanRepository = TestWeekPlanRepository()
    val templateApplier = TemplateApplier(
        weekPlanRepository,
        TemplateTestData.Helpers.helpersMap,
    )

    beforeEach { weekPlanRepository.reset() }

    context("Template cutting dates") {
        val schedulingStart = YearWeek(1919, 12)
        val schedulingEnd = YearWeek(1919, 15)
        val weekShiftCount = TemplateTestData.WeekTemplates.week.sumOf { it.shifts.size }

        context("start date") {
            test("template is before scheduling") {
                val template = TemplateTestData.Templates.template
                    .copy(start = YearWeek(1919, 3))

                templateApplier.applyTemplate(schedulingStart, schedulingEnd, template)

                val expectedWeekCount = 4
                val expectedShiftCount = expectedWeekCount * weekShiftCount
                weekPlanRepository.shiftList shouldHaveSize expectedShiftCount
                weekPlanRepository.firstShiftStart() shouldBe YearWeekDay(schedulingStart, DayOfWeek.MONDAY)
                weekPlanRepository.lastShiftStart() shouldBe YearWeekDay(schedulingEnd, DayOfWeek.SUNDAY)
            }

            test("template in between scheduling") {
                val template = TemplateTestData.Templates.template
                    .copy(start = YearWeek(1919, 13))

                templateApplier.applyTemplate(schedulingStart, schedulingEnd, template)

                val expectedWeekCount = 3
                val expectedShiftCount = expectedWeekCount * weekShiftCount
                weekPlanRepository.shiftList shouldHaveSize expectedShiftCount
                weekPlanRepository.firstShiftStart() shouldBe YearWeekDay(1919, 13, DayOfWeek.MONDAY)
                weekPlanRepository.lastShiftStart() shouldBe YearWeekDay(schedulingEnd, DayOfWeek.SUNDAY)
            }

            test("template is after scheduling") {
                val template = TemplateTestData.Templates.template
                    .copy(start = YearWeek(1919, 16))

                templateApplier.applyTemplate(schedulingStart, schedulingEnd, template)

                weekPlanRepository.shiftList.shouldBeEmpty()
            }
        }

        // TODO don't test old logic, but move to newest when possible
        xcontext("switches to the correct template") {
            test("two templates, schduling start is after second start") {
                val template = TemplateTestData.Templates.template
            }

            test("none applied if scheduling end is before only start") {
                val template = TemplateTestData.Templates.template
            }

            test("scheduling touches two templates") {

            }
        }
    }

    context("context apply week templates") {
        test("Has correct schedule") {
            val schedulingStart = TemplateTestData.Templates.template.start
            val schedulingEnd = schedulingStart

            templateApplier.applyTemplate(schedulingStart, schedulingEnd, TemplateTestData.Templates.template)

            val mondayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.MONDAY))
            val tuesdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.TUESDAY))
            val wednesdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.WEDNESDAY))
            val thurdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.THURSDAY))
            val fridayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.FRIDAY))
            val saturdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.SATURDAY))
            val sundayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.SUNDAY))

            val expecteedShifts = TemplateTestData.TestRepositoryShifts(schedulingStart)
            mondayShifts shouldBe listOf(expecteedShifts.monday_day)
            tuesdayShifts shouldBe listOf(expecteedShifts.tuesday_day, expecteedShifts.tueday_night)
            wednesdayShifts shouldBe listOf(expecteedShifts.wednesday_day)
            thurdayShifts shouldBe listOf(expecteedShifts.thursday_night)
            fridayShifts shouldBe listOf()
            saturdayShifts shouldBe listOf(expecteedShifts.saturday_day)
            sundayShifts shouldBe listOf(expecteedShifts.sunday_day)
        }

        test("Book correct helper") {
            val schedulingStart = TemplateTestData.Templates.template.start
            val schedulingEnd = schedulingStart

            templateApplier.applyTemplate(schedulingStart, schedulingEnd, TemplateTestData.Templates.template)

            val mondayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.MONDAY))
            val tuesdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.TUESDAY))
            val wednesdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.WEDNESDAY))
            val thurdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.THURSDAY))
            val fridayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.FRIDAY))
            val saturdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.SATURDAY))
            val sundayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.SUNDAY))

            mondayHelpers shouldBe listOf(HelperBooking.PermanentHelper("jazz"))
            tuesdayHelpers shouldBe listOf(HelperBooking.NoBooking, HelperBooking.PermanentHelper("rockabilly"))
            wednesdayHelpers shouldBe listOf(HelperBooking.PermanentHelper("blues"))
            thurdayHelpers shouldBe listOf(HelperBooking.PermanentHelper("metal"))
            fridayHelpers shouldBe listOf()
            saturdayHelpers shouldBe listOf(HelperBooking.NoBooking)
            sundayHelpers shouldBe listOf(HelperBooking.PermanentHelper("hiphop"))
        }
    }

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
