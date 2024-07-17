package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.MemoryWeekSynchronizationRepository
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.TestShiftRespository
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly

class CreateShiftTest : FunSpec({
    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekSynchronizationRepository = MemoryWeekSynchronizationRepository()
    val weekPlanService = WeekPlanService(salarySystemRepository, shiftRepository, weekSynchronizationRepository)


    val shift1Start = YearWeekDayAtTime.parseUnsafe("2024-W13-1T13:30")
    val shift1End = YearWeekDayAtTime.parseUnsafe("2024-W13-1T14:30")

    test("create shift") {
        weekPlanService.createShift(shift1Start, shift1End)

        weekSynchronizationRepository.weeksToSynchronize(
            shift1Start.yearWeekDay.yearWeek..shift1Start.yearWeekDay.yearWeek
        ).shouldBeEmpty()
        salarySystemRepository.shiftList
            .map { SynchronizationTest.NoIdShift(it.helperId, it.start, it.end) }
            .shouldContainExactly(
                listOf(
                    SynchronizationTest.NoIdShift(HelperBooking.NoBooking, shift1Start, shift1End),
                )
            )
        shiftRepository.shiftList
            .map { SynchronizationTest.NoIdShift(it.helperId, it.start, it.end) }
            .shouldContainExactly(
                listOf(
                    SynchronizationTest.NoIdShift(HelperBooking.NoBooking, shift1Start, shift1End),
                )
            )
    }

    test("shift not created in shift repository") {}
})
