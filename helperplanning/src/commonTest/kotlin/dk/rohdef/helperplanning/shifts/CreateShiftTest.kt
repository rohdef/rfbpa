package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.MemoryWeekSynchronizationRepository
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.TestShiftRespository
import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
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
        // TODO: 21/07/2024 rohdef - mark synchronized?
        weekPlanService.createShift(shift1Start, shift1End)

        salarySystemRepository.shiftList
            .shouldContainExactly(
                listOf(
                    Shift(
                        HelperBooking.NoBooking,
                        generateTestShiftId(shift1Start, shift1End),
                        shift1Start,
                        shift1End
                    ),
                )
            )
        val all2024Weeks = YearWeek(2024, 1)..YearWeek(2024, 52)
        weekSynchronizationRepository.weeksToSynchronize(all2024Weeks) shouldContainExactly all2024Weeks
    }

    test("shift not created in shift repository") {}
})
