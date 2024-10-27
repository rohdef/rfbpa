package dk.rohdef.helperplanning.templates

import arrow.core.nonEmptyListOf
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.helpers.HelperTestData
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek

class TemplateApplierTest : FunSpec({
    val memoryHelpers = MemoryHelpersRepository()
    val memoryWeekPlanRepository = MemorySalarySystemRepository()
    val weekPlanRepository = TestSalarySystemRepository(memoryWeekPlanRepository)

    val templateApplier = TemplateApplier(
        weekPlanRepository,
        memoryHelpers,
    )

    beforeEach {
        weekPlanRepository.reset()
        memoryHelpers.reset()

        HelperTestData.allHelpers.forEach { memoryHelpers.create(it) }
    }

    context("Template cutting dates") {
        val schedulingStart = YearWeek(1919, 12)
        val schedulingEnd = YearWeek(1919, 15)
        val weekShiftCount = TemplateTestData.WeekTemplates.complexWeek.shifts.size

        context("start date") {
            test("template is before scheduling") {
                val template = TemplateTestData.Templates.template_single_week_template_complex
                    .copy(start = YearWeek(1919, 3))

                templateApplier.applyTemplates(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    schedulingStart..schedulingEnd,
                    nonEmptyListOf(template),
                )

                val expectedWeekCount = 4
                val expectedShiftCount = expectedWeekCount * weekShiftCount
                weekPlanRepository.shiftList shouldHaveSize expectedShiftCount
                weekPlanRepository.firstShiftStart() shouldBe YearWeekDay(schedulingStart, DayOfWeek.MONDAY)
                weekPlanRepository.lastShiftStart() shouldBe YearWeekDay(schedulingEnd, DayOfWeek.SUNDAY)
            }

            test("template in between scheduling") {
                val template = TemplateTestData.Templates.template_single_week_template_complex
                    .copy(start = YearWeek(1919, 13))

                templateApplier.applyTemplates(
                    PrincipalsTestData.FiktivusMaximus.templateAdmin,
                    schedulingStart..schedulingEnd,
                    nonEmptyListOf(template)
                )

                val expectedWeekCount = 3
                val expectedShiftCount = expectedWeekCount * weekShiftCount
                weekPlanRepository.shiftList shouldHaveSize expectedShiftCount
                weekPlanRepository.firstShiftStart() shouldBe YearWeekDay(1919, 13, DayOfWeek.MONDAY)
                weekPlanRepository.lastShiftStart() shouldBe YearWeekDay(schedulingEnd, DayOfWeek.SUNDAY)
            }

            test("template is after scheduling") {
                val template = TemplateTestData.Templates.template_single_week_template_complex
                    .copy(start = YearWeek(1919, 16))

                templateApplier.applyTemplates(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    schedulingStart..schedulingEnd,
                    nonEmptyListOf(template),
                )

                weekPlanRepository.shiftList.shouldBeEmpty()
            }
        }

        // TODO don't test old logic, but move to newest when possible
        xcontext("switches to the correct template") {
            test("two templates, schduling start is after second start") {
                val template = TemplateTestData.Templates.template_single_week_template_complex
            }

            test("none applied if scheduling end is before only start") {
                val template = TemplateTestData.Templates.template_single_week_template_complex
            }

            test("scheduling touches two templates") {

            }
        }
    }

    context("context apply week template") {
        test("Has correct schedule") {
            val schedulingStart = TemplateTestData.Templates.template_single_week_template_complex.start
            val schedulingEnd = schedulingStart

            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.templateAdmin,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(TemplateTestData.Templates.template_single_week_template_complex),
            )

            val mondayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.MONDAY))
            val tuesdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.TUESDAY))
            val wednesdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.WEDNESDAY))
            val thurdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.THURSDAY))
            val fridayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.FRIDAY))
            val saturdayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.SATURDAY))
            val sundayShifts = weekPlanRepository.shiftListOnDay(YearWeekDay(schedulingStart, DayOfWeek.SUNDAY))

            val expectedShifts = TemplateTestData.TestRepositoryShifts(schedulingStart)
            mondayShifts shouldBe listOf(expectedShifts.monday_day)
            tuesdayShifts shouldBe listOf(expectedShifts.tuesday_day, expectedShifts.tueday_night)
            wednesdayShifts shouldBe listOf(expectedShifts.wednesday_day)
            thurdayShifts shouldBe listOf(expectedShifts.thursday_night)
            fridayShifts shouldBe listOf()
            saturdayShifts shouldBe listOf(expectedShifts.saturday_day)
            sundayShifts shouldBe listOf(expectedShifts.sunday_day)
        }

        test("Book correct helper") {
            val schedulingStart = TemplateTestData.Templates.template_single_week_template_complex.start
            val schedulingEnd = schedulingStart

            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(TemplateTestData.Templates.template_single_week_template_complex),
            )

            val mondayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.MONDAY))
            val tuesdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.TUESDAY))
            val wednesdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.WEDNESDAY))
            val thurdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.THURSDAY))
            val fridayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.FRIDAY))
            val saturdayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.SATURDAY))
            val sundayHelpers = weekPlanRepository.helpersOnDay(YearWeekDay(schedulingStart, DayOfWeek.SUNDAY))

            // TODO: 25/06/2024 rohdef - the dealing with Helper.ID typing should probably be improved
            mondayHelpers shouldBe listOf(HelperBooking.Booked(HelperTestData.permanentJazz.id))
            tuesdayHelpers shouldBe listOf(
                HelperBooking.NoBooking,
                HelperBooking.Booked(HelperTestData.permanentRockabilly.id)
            )
            wednesdayHelpers shouldBe listOf(HelperBooking.Booked(HelperTestData.permanentBlues.id))
            thurdayHelpers shouldBe listOf(HelperBooking.Booked(HelperTestData.permanentMetal.id))
            fridayHelpers shouldBe listOf()
            saturdayHelpers shouldBe listOf(HelperBooking.NoBooking)
            sundayHelpers shouldBe listOf(HelperBooking.Booked(HelperTestData.permanentHipHop.id))
        }
    }

    context("rotating week templates") {
        val template = TemplateTestData.Templates.template_three_week_rotation_simple
        val rotations = 3
        val weeksNeeded = rotations * template.weeks.size

        val schedulingStart = YearWeek(1931, 12)
        val endWeek = schedulingStart.week + weeksNeeded - 1
        val schedulingEnd = YearWeek(schedulingStart.year, endWeek)

        test("Scheduling starts at same date as template start") {
            val template = template.copy(start = schedulingStart)

            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.templateAdmin,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(template),
            )

            val weeksInInterval = (schedulingStart..schedulingEnd).map { TemplateTestData.TestRepositoryShifts(it) }
            val expectedMondays =
                listOf(weeksInInterval[0], weeksInInterval[3], weeksInInterval[6]).map { it.monday_day }
            val expectedWednesdays =
                listOf(weeksInInterval[1], weeksInInterval[4], weeksInInterval[7]).map { it.wednesday_day }
            val expectedSaturdays =
                listOf(weeksInInterval[2], weeksInInterval[5], weeksInInterval[8]).map { it.saturday_day }

            val expectedShifts = expectedMondays + expectedWednesdays + expectedSaturdays
            weekPlanRepository.shiftList shouldContainExactlyInAnyOrder expectedShifts
        }

        test("Scheduling starts after template start") {
            val weekStart = schedulingStart.week - template.weeks.size + 1
            val template = template.copy(start = schedulingStart.copy(week = weekStart))

            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(template),
            )

            val weeksInInterval = (schedulingStart..schedulingEnd).map { TemplateTestData.TestRepositoryShifts(it) }
            val expectedMondays =
                listOf(weeksInInterval[1], weeksInInterval[4], weeksInInterval[7]).map { it.monday_day }
            val expectedWednesdays =
                listOf(weeksInInterval[2], weeksInInterval[5], weeksInInterval[8]).map { it.wednesday_day }
            val expectedSaturdays =
                listOf(weeksInInterval[0], weeksInInterval[3], weeksInInterval[6]).map { it.saturday_day }

            val expectedShifts = expectedMondays + expectedWednesdays + expectedSaturdays
            weekPlanRepository.shiftList shouldContainExactlyInAnyOrder expectedShifts
        }

        test("Scheduling starts before template start") {
            val weekStart = schedulingStart.week + template.weeks.size - 1
            val template = template.copy(start = schedulingStart.copy(week = weekStart))

            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.templateAdmin,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(template),
            )

            val weeksInInterval = (schedulingStart..schedulingEnd).map { TemplateTestData.TestRepositoryShifts(it) }
            val expectedMondays =
                listOf(weeksInInterval[2], weeksInInterval[5], weeksInInterval[8]).map { it.monday_day }
            val expectedWednesdays = listOf(weeksInInterval[3], weeksInInterval[6]).map { it.wednesday_day }
            val expectedSaturdays = listOf(weeksInInterval[4], weeksInInterval[7]).map { it.saturday_day }

            val expectedShifts = expectedMondays + expectedWednesdays + expectedSaturdays
            weekPlanRepository.shiftList shouldContainExactlyInAnyOrder expectedShifts
        }
    }

    context("Principal") {
        val schedulingStart = YearWeek(1919, 12)
        val schedulingEnd = YearWeek(1919, 13)

        test("Distinguishing between subjects") {
            val template1 = TemplateTestData.Templates.template_monday
            val template2 = TemplateTestData.Templates.template_wednesday

            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(template1),
            )
            templateApplier.applyTemplates(
                PrincipalsTestData.RealisMinimalis.allRoles,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(template2),
            )

            val maximusShifts =
                weekPlanRepository.shifts(PrincipalsTestData.FiktivusMaximus.subject, schedulingStart..schedulingEnd)
                    .shouldBeRight()
            val realisShifts =
                weekPlanRepository.shifts(PrincipalsTestData.RealisMinimalis.subject, schedulingStart..schedulingEnd)
                    .shouldBeRight()

            maximusShifts.flatMap { it.allShifts }
                .map { it.start.dayOfWeek } shouldContainExactly listOf(DayOfWeek.MONDAY, DayOfWeek.MONDAY)
            realisShifts.flatMap { it.allShifts }
                .map { it.start.dayOfWeek } shouldContainExactly listOf(DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY)
        }

        test("Requires the role ${RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN}") {
            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(TemplateTestData.Templates.template_monday)
            )
                .shouldBeRight()

            templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.templateAdmin,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(TemplateTestData.Templates.template_monday)
            )
                .shouldBeRight()

            val shiftAdminError = templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.shiftAdmin,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(TemplateTestData.Templates.template_monday)
            )
                .shouldBeLeft()

            val helperAdminError = templateApplier.applyTemplates(
                PrincipalsTestData.FiktivusMaximus.helperAdmin,
                schedulingStart..schedulingEnd,
                nonEmptyListOf(TemplateTestData.Templates.template_monday)
            )
                .shouldBeLeft()

            shiftAdminError shouldBe TemplateApplier.Error.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN,
                PrincipalsTestData.FiktivusMaximus.shiftAdmin.roles,
            )

            helperAdminError shouldBe TemplateApplier.Error.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN,
                PrincipalsTestData.FiktivusMaximus.helperAdmin.roles,
            )
        }
    }
})
