package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.MemoryWeekSynchronizationRepository
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.TestShiftRespository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek

private fun interface ShiftBuilderOnDay {
    fun start(hours: Int, minutes: Int): ShiftBuilderAtStart
}

private fun interface ShiftBuilderAtStart {
    fun end(hours: Int, minutes: Int): Shift
}

class SynchronizationTest : FunSpec({
    fun createTestShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Shift {
        return Shift(
            HelperBooking.NoBooking,
            generateTestShiftId(start, end),
            start,
            end,
        )
    }

    fun YearWeek.shift(dayOfWeek: DayOfWeek): ShiftBuilderOnDay {
        return ShiftBuilderOnDay { startHours, startMinutes ->
            ShiftBuilderAtStart { endHours, endMinutes ->
                this.atDayOfWeek(dayOfWeek).let {
                    createTestShift(
                        it.atTime(startHours, startMinutes),
                        it.atTime(endHours, endMinutes),
                    )
                }
            }
        }
    }

    val year2024Week13 = YearWeek(2024, 13)

    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekSynchronizationRepository = MemoryWeekSynchronizationRepository()
    val weekPlanService = WeekPlanService(salarySystemRepository, shiftRepository, weekSynchronizationRepository)

    val shift1 = year2024Week13.shift(DayOfWeek.MONDAY).start(13, 30).end(14, 30)
    val shift2 = year2024Week13.shift(DayOfWeek.WEDNESDAY).start(17, 30).end(21, 30)
    val shift3 = year2024Week13.shift(DayOfWeek.THURSDAY).start(9, 45).end(15, 15)
    val week13Shifts = listOf(
        shift1,
        shift2,
        shift3,
    )

    val shiftNotInSystem = year2024Week13.shift(DayOfWeek.SATURDAY).start(8, 0).end(15, 45)

    beforeEach {
        weekSynchronizationRepository.reset()
        shiftRepository.reset()

        salarySystemRepository.apply {
            reset()

            createShift(shift1.start, shift1.end)
            createShift(shift2.start, shift2.end)
            createShift(shift3.start, shift3.end)
        }
    }

    context("Single week") {
        test("No syncronized") {
            shiftRepository.shiftList.shouldBeEmpty()
            weekSynchronizationRepository.synchronizationState(year2024Week13) shouldBe WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE

            weekPlanService.synchronize(year2024Week13)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder week13Shifts
            weekSynchronizationRepository.synchronizationState(year2024Week13) shouldBe WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }

        test("Aleady synchronized - synchronization only happens when requested") {
            weekPlanService.synchronize(year2024Week13)
            salarySystemRepository.addShift(shiftNotInSystem)

            weekPlanService.synchronize(year2024Week13)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder week13Shifts
            weekSynchronizationRepository.synchronizationState(year2024Week13) shouldBe WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }

        test("Synchronized but marked for synchronization") {
            weekPlanService.synchronize(year2024Week13)
            weekSynchronizationRepository.markForSynchronization(year2024Week13)
            salarySystemRepository.addShift(shiftNotInSystem)

            weekPlanService.synchronize(year2024Week13)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + shiftNotInSystem)
            weekSynchronizationRepository.synchronizationState(year2024Week13) shouldBe WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }

        test("Create shift marks for synchhronization") {
            val targetYearWeek = shiftNotInSystem.start.yearWeek
            weekSynchronizationRepository.markSynchronized(targetYearWeek)
            salarySystemRepository.addShift(shiftNotInSystem)

            weekPlanService.createShift(shiftNotInSystem.start, shiftNotInSystem.end)

            salarySystemRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + shiftNotInSystem)
            weekSynchronizationRepository.synchronizationState(targetYearWeek) shouldBe WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE
        }
    }

    context("Multiple weeks") {
        val year2024Week14 = YearWeek(2024, 14)
        val year2024Week15 = YearWeek(2024, 15)
        val year2024Week16 = YearWeek(2024, 16)

        val shift9Start = year2024Week16.atDayOfWeek(DayOfWeek.WEDNESDAY).atTime(9, 45)
        val shift9End = year2024Week16.atDayOfWeek(DayOfWeek.FRIDAY).atTime(23, 45)
        val shift4 = year2024Week14.shift(DayOfWeek.MONDAY).start(13, 30).end(14, 30)
        val shift5 = year2024Week14.shift(DayOfWeek.MONDAY).start(19, 0).end(20, 45)
        val shift6 = year2024Week14.shift(DayOfWeek.WEDNESDAY).start(8, 15).end(21, 45)
        val shift7 = year2024Week15.shift(DayOfWeek.MONDAY).start(11, 15).end(15, 30)
        val shift8 = year2024Week15.shift(DayOfWeek.THURSDAY).start(5, 45).end(9, 45)
        val shift9 = createTestShift(shift9Start, shift9End)
        val shift10 = year2024Week16.shift(DayOfWeek.SATURDAY).start(8, 15).end(10, 30)
        val shift11 = year2024Week16.shift(DayOfWeek.SATURDAY).start(16, 0).end(21, 15)
        val shift12 = year2024Week16.shift(DayOfWeek.SATURDAY).start(9, 0).end(22, 0)
        val week14Shifts = listOf(shift4, shift5, shift6)
        val week15Shifts = listOf(shift7, shift8)
        val week16Shifts = listOf(shift9, shift10, shift11, shift12)
        val additionalShifts = week14Shifts + week15Shifts + week16Shifts

        val shiftsToAdd = (week13Shifts + additionalShifts).map { it.start.yearWeek }
            .distinct()
            .map { it.atDayOfWeek(DayOfWeek.TUESDAY) }
            .map { createTestShift(it.atTime(4, 0), it.atTime(12, 0)) }

        beforeEach {
            additionalShifts.forEach { salarySystemRepository.createShift(it.start, it.end) }
        }

        test("week interval") {
            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16).values
            synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)

            weekPlanService.synchronize(year2024Week13..year2024Week16)

            val synchronizationStatesSync =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16).values
            synchronizationStatesSync shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + additionalShifts)
        }

        test("weeks in interval with interruptions") {
            // TODO make to test factory repeater thing
            weekSynchronizationRepository.markSynchronized(year2024Week14)
            weekSynchronizationRepository.markSynchronized(year2024Week15)

            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16)
            synchronizationStates shouldContainExactly mapOf(
                year2024Week13 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                year2024Week14 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week15 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week16 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
            )

            shiftsToAdd.forEach { salarySystemRepository.addShift(it) }

            weekPlanService.synchronize(year2024Week13..year2024Week16)

            val synchronizationStatesSync =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16).values
            synchronizationStatesSync shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED)

            val tuesdays = shiftRepository.shiftList
                .filter { it.start.dayOfWeek == DayOfWeek.TUESDAY }
                .map { it.start.yearWeek }
            tuesdays shouldContainExactlyInAnyOrder listOf(year2024Week13, year2024Week16)
        }

        test("Multiple weeks - one fails in salary system, rest should still sync") {
            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16).values
            synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)
            salarySystemRepository.addShiftsPrerunner { if (it == year2024Week15) throw RuntimeException("Week 15 must fail") }

            weekPlanService.synchronize(year2024Week13..year2024Week16)

            val updatedSynchronizationStates =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16)
            updatedSynchronizationStates shouldContainExactly mapOf(
                year2024Week13 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week14 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week15 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                year2024Week16 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
            )
            shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + additionalShifts - week15Shifts)
        }

        test("Multiple weeks - one fails in shift system, rest should still sync") {
            shiftRepository.addCreateShiftPrerunner { if (it == shift5) throw RuntimeException("Week 14 must fail") }
            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16).values
            synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)

            weekPlanService.synchronize(year2024Week13..year2024Week16)

            val updatedSynchronizationStates =
                weekSynchronizationRepository.synchronizationStates(year2024Week13..year2024Week16)
            updatedSynchronizationStates shouldContainExactly mapOf(
                year2024Week13 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week14 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                year2024Week15 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week16 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
            )
            shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + additionalShifts - shift5)
        }
    }

    xcontext("Error scenarios") {
        test("Fail due to salary system") {}

        test("Fail due to shifts repository") {}

        test("Fail as domain failure") {}

        test("Fail as exception") {}
    }

    xcontext("Mismatch scenarios") {
        // Shift missing in salary repository
        // - check on the salary ID (could have been moved to different week, yuck)
        // - if really not existing, remove
        // - mark synced

        // shift missing in shifts repository
        // - just add
        // - mark synced

        // shift already exists, but different
        // - salary rules, update shift repo
        // - mark synced

        // booking changed (is this part of it? Yes I think so! Scenario for when booking sync is added)
        // - salary rules, update booking repo
        // - mark synced

        // consideration, does illness affect this
        // consideration, what about supplements (tillæg)
        //      supplements might have issues with shifts that has been paid
        //      shift can still be confirmed existing (error confirms existence)
    }

    xcontext("Week already in the system - introduce the might be out of date context") {
        test("New shift") {
            // TODO test already in Single week - move here and adjust accordingly
        }

        test("Change in shift") {

        }

        test("Deleted shift") {

        }

        test("Change booking") {

        }

        test("Change supplements (tillæg)") {

        }
    }
})
