package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.TestShiftRespository
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class SynchronizationTest : FunSpec({
    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekPlanService = WeekPlanService(salarySystemRepository, shiftRepository)

    val shift1Start = YearWeekDayAtTime.parseUnsafe("2024-W13-1T13:30")
    val shift1End = YearWeekDayAtTime.parseUnsafe("2024-W13-1T14:30")
    val shift2Start = YearWeekDayAtTime.parseUnsafe("2024-W13-3T17:30")
    val shift2End = YearWeekDayAtTime.parseUnsafe("2024-W13-3T21:30")
    val shift3Start = YearWeekDayAtTime.parseUnsafe("2024-W13-4T17:30")
    val shift3End = YearWeekDayAtTime.parseUnsafe("2024-W13-5T17:30")

    beforeEach {
        shiftRepository.reset()

        salarySystemRepository.apply {
            reset()

            createShift(shift1Start, shift1End)
            createShift(shift2Start, shift2End)
            createShift(shift3Start, shift3End)
        }
    }

    context("Non-synchronized weeks") {
        test("one week") {
            weekPlanService.sync(YearWeek(2024, 13))

            val noIdShifts = shiftRepository.shiftList.map { NoIdShift.fromShift(it) }
            noIdShifts shouldContainExactlyInAnyOrder listOf(
                NoIdShift(HelperBooking.NoBooking, shift1Start, shift1End),
                NoIdShift(HelperBooking.NoBooking, shift2Start, shift2End),
                NoIdShift(HelperBooking.NoBooking, shift3Start, shift3End),
            )
        }


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
            NoIdShift(HelperBooking.NoBooking, shift4Start, shift4End),
            NoIdShift(HelperBooking.NoBooking, shift5Start, shift5End),
            NoIdShift(HelperBooking.NoBooking, shift6Start, shift6End),
            NoIdShift(HelperBooking.NoBooking, shift7Start, shift7End),
            NoIdShift(HelperBooking.NoBooking, shift8Start, shift8End),
            NoIdShift(HelperBooking.NoBooking, shift9Start, shift9End),
            NoIdShift(HelperBooking.NoBooking, shift10Start, shift10End),
            NoIdShift(HelperBooking.NoBooking, shift11Start, shift11End),
            NoIdShift(HelperBooking.NoBooking, shift12Start, shift12End),
        )

        test("week interval") {
            additionalShifts.forEach { salarySystemRepository.createShift(it.start, it.end) }
        }

        test("weeks in interval with interruption") {
            additionalShifts.forEach { salarySystemRepository.createShift(it.start, it.end) }
            shiftRepository.createShift(shift7Start, shift7End)
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
