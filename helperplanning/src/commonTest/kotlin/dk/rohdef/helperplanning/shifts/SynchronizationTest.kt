package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.MemoryWeekSynchronizationRepository
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.TestShiftRespository
import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.*

class SynchronizationTest : FunSpec({
    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekSynchronizationRepository = MemoryWeekSynchronizationRepository()
    val weekPlanService = WeekPlanService(salarySystemRepository, shiftRepository, weekSynchronizationRepository)

    val shift1Start = YearWeekDayAtTime.parseUnsafe("2024-W13-1T13:30")
    val shift1End = YearWeekDayAtTime.parseUnsafe("2024-W13-1T14:30")
    val shift2Start = YearWeekDayAtTime.parseUnsafe("2024-W13-3T17:30")
    val shift2End = YearWeekDayAtTime.parseUnsafe("2024-W13-3T21:30")
    val shift3Start = YearWeekDayAtTime.parseUnsafe("2024-W13-4T17:30")
    val shift3End = YearWeekDayAtTime.parseUnsafe("2024-W13-5T17:30")

    val shiftNotInSystem = Shift(
        HelperBooking.NoBooking,
        YearWeekDayAtTime.parseUnsafe("2024-W13-6T08:00"),
        YearWeekDayAtTime.parseUnsafe("2024-W13-6T15:45"),
    )

    beforeEach {
        shiftRepository.reset()

        salarySystemRepository.apply {
            reset()

            createShift(shift1Start, shift1End)
            createShift(shift2Start, shift2End)
            createShift(shift3Start, shift3End)
        }
    }

    fun createTestShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Shift {
        return Shift(
            HelperBooking.NoBooking,
            generateTestShiftId(start, end),
            start,
            end,
        )
    }

    val year2024Weeks = YearWeek(2024, 1)..YearWeek(2024, 52)

    context("Single week") {
        val year2024Week13 = YearWeek(2024, 13)

        test("No syncronized") {
            shiftRepository.shiftList.shouldBeEmpty()
            weekSynchronizationRepository.weeksToSynchronize(year2024Weeks) shouldContain year2024Week13

            weekPlanService.sync(year2024Week13)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder listOf(
                createTestShift(shift1Start, shift1End),
                createTestShift(shift2Start, shift2End),
                createTestShift(shift3Start, shift3End),
            )
            weekSynchronizationRepository.weeksToSynchronize(year2024Weeks) shouldNotContain year2024Week13
        }

        test("Aleady synchronized - synchronization only happens when requested") {
            weekSynchronizationRepository.markSynchronized(year2024Week13)
            salarySystemRepository.addShift(shiftNotInSystem)

            weekPlanService.sync(year2024Week13)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder listOf(
                createTestShift(shift1Start, shift1End),
                createTestShift(shift2Start, shift2End),
                createTestShift(shift3Start, shift3End),
            )
            weekSynchronizationRepository.weeksToSynchronize(year2024Weeks) shouldNotContain year2024Week13
        }

        test("Synchronized but marked for synchronization") {
            weekPlanService.sync(year2024Week13)
            weekSynchronizationRepository.markForSynchronization(year2024Week13)
            salarySystemRepository.addShift(shiftNotInSystem)

            weekPlanService.sync(year2024Week13)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder listOf(
                createTestShift(shift1Start, shift1End),
                createTestShift(shift2Start, shift2End),
                createTestShift(shift3Start, shift3End),
                shiftNotInSystem,
            )
            weekSynchronizationRepository.weeksToSynchronize(year2024Weeks) shouldNotContain year2024Week13
        }

        test("Create shift marks for synchhronization") {
            weekSynchronizationRepository.markSynchronized(year2024Week13)
            salarySystemRepository.addShift(shiftNotInSystem)

            weekPlanService.createShift(shiftNotInSystem.start, shiftNotInSystem.end)

            salarySystemRepository.shiftList shouldContainExactlyInAnyOrder listOf(
                createTestShift(shift1Start, shift1End),
                createTestShift(shift2Start, shift2End),
                createTestShift(shift3Start, shift3End),
                shiftNotInSystem,
            )
            weekSynchronizationRepository.weeksToSynchronize(year2024Weeks) shouldNotContain year2024Week13
        }
    }

    context("Non-synchronized weeks") {
        val shift4Start = YearWeekDayAtTime.parseUnsafe("2024-W14-1T13:30")
        val shift4End = YearWeekDayAtTime.parseUnsafe("2024-W14-1T14:30")
        val shift5Start = YearWeekDayAtTime.parseUnsafe("2024-W14-1T13:30")
        val shift5End = YearWeekDayAtTime.parseUnsafe("2024-W14-1T14:30")
        val shift6Start = YearWeekDayAtTime.parseUnsafe("2024-W14-1T13:30")
        val shift6End = YearWeekDayAtTime.parseUnsafe("2024-W14-1T14:30")
        val shift7Start = YearWeekDayAtTime.parseUnsafe("2024-W15-1T13:30")
        val shift7End = YearWeekDayAtTime.parseUnsafe("2024-W15-1T14:30")
        val shift8Start = YearWeekDayAtTime.parseUnsafe("2024-W15-1T13:30")
        val shift8End = YearWeekDayAtTime.parseUnsafe("2024-W15-1T14:30")
        val shift9Start = YearWeekDayAtTime.parseUnsafe("2024-W16-1T13:30")
        val shift9End = YearWeekDayAtTime.parseUnsafe("2024-W16-1T14:30")
        val shift10Start = YearWeekDayAtTime.parseUnsafe("2024-W16-1T13:30")
        val shift10End = YearWeekDayAtTime.parseUnsafe("2024-W16-1T14:30")
        val shift11Start = YearWeekDayAtTime.parseUnsafe("2024-W16-1T13:30")
        val shift11End = YearWeekDayAtTime.parseUnsafe("2024-W16-1T14:30")
        val shift12Start = YearWeekDayAtTime.parseUnsafe("2024-W16-1T13:30")
        val shift12End = YearWeekDayAtTime.parseUnsafe("2024-W16-1T14:30")
        val additionalShifts = listOf(
            createTestShift(shift4Start, shift4End),
            createTestShift(shift5Start, shift5End),
            createTestShift(shift6Start, shift6End),
            createTestShift(shift7Start, shift7End),
            createTestShift(shift8Start, shift8End),
            createTestShift(shift9Start, shift9End),
            createTestShift(shift10Start, shift10End),
            createTestShift(shift11Start, shift11End),
            createTestShift(shift12Start, shift12End),
        )

        test("week interval") {
            additionalShifts.forEach { salarySystemRepository.createShift(it.start, it.end) }
        }

        xtest("weeks in interval with interruption") {
            TODO()
//            additionalShifts.forEach { salarySystemRepository.createShift(it.start, it.end) }
//            shiftRepository.createShift(shift7Start, shift7End)
        }
    }

    context("Week already in the system") {
        val salarySystemRepository = TestSalarySystemRepository().apply {
            createShift(
                YearWeekDayAtTime.parseUnsafe("2024-W13-1T13:30"),
                YearWeekDayAtTime.parseUnsafe("2024-W13-1T14:30"),
            )
            createShift(
                YearWeekDayAtTime.parseUnsafe("2024-W13-3T13:30"),
                YearWeekDayAtTime.parseUnsafe("2024-W13-3T14:30"),
            )
        }

        test("New shift") {

        }

        test("Change in shift") {

        }

        test("Deleted shift") {

        }
    }

    xtest("Multiple weeks")

    xtest("Multiple weeks - one fails, rest should still sync")

    xtest("Fail due to salary system")

    xtest("Fail due to shifts repository")

    xtest("Fail as domain failure")

    xtest("Fail as exception")
}) {
    internal data class NoIdShift(
        val helperId: HelperBooking,
        val start: YearWeekDayAtTime,
        val end: YearWeekDayAtTime,
    ) {
        companion object {
            fun fromShift(shift: Shift): NoIdShift {
                return NoIdShift(
                    shift.helperId,
                    shift.start,
                    shift.end,
                )
            }
        }
    }
}
