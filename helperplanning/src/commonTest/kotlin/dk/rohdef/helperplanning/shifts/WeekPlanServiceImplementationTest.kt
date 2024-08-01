package dk.rohdef.helperplanning.shifts

import arrow.core.left
import arrow.core.right
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.TestShiftRespository
import dk.rohdef.helperplanning.TestWeekSynchronizationRepository
import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek

class WeekPlanServiceImplementationTest : FunSpec({
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

    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekSynchronizationRepository = TestWeekSynchronizationRepository()
    val weekPlanService = WeekPlanServiceImplementation(salarySystemRepository, shiftRepository, weekSynchronizationRepository)

    val year2024Week8 = YearWeek(2024, 8)
    val year2024Week9 = YearWeek(2024, 9)
    val year2024Week10 = YearWeek(2024, 10)

    val week8Shift1 = year2024Week8.shift(DayOfWeek.MONDAY).start(13, 30).end(14, 30)
    val week8Shift2 = year2024Week8.shift(DayOfWeek.WEDNESDAY).start(17, 30).end(21, 30)
    val week8Shifts = listOf(week8Shift1, week8Shift2)

    val week9Shift1 = year2024Week9.shift(DayOfWeek.TUESDAY).start(8, 0).end(19, 15)
    val week9Shifts = listOf(week9Shift1)

    val week10Shift1 = year2024Week10.shift(DayOfWeek.WEDNESDAY).start(10, 45).end(20, 30)
    val week10Shift2 = year2024Week10.shift(DayOfWeek.SUNDAY).start(6, 0).end(23, 0)
    val week10Shifts = listOf(week10Shift1, week10Shift2)

    val allShiftsInSystem = week8Shifts + week9Shifts + week10Shifts

    val week8ShiftNotInSystem = year2024Week8.shift(DayOfWeek.SATURDAY).start(8, 0).end(15, 45)
    val week9ShiftNotInSystem = year2024Week9.shift(DayOfWeek.MONDAY).start(21, 0).end(22, 45)
    val week10ShiftNotInSystem = year2024Week10.shift(DayOfWeek.THURSDAY).start(8, 0).end(15, 45)

    val shiftsNotInSystem = listOf(week8ShiftNotInSystem, week9ShiftNotInSystem, week10ShiftNotInSystem)

    beforeEach {
        salarySystemRepository.reset()
        shiftRepository.reset()
        weekSynchronizationRepository.reset()

        allShiftsInSystem.forEach { salarySystemRepository.addShift(it) }
        shiftsNotInSystem.forEach { salarySystemRepository.addShift(it) }
        allShiftsInSystem.forEach { shiftRepository.createOrUpdate(it) }

        weekSynchronizationRepository.markSynchronized(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week10).shouldBeRight()
    }

    test("should synchronize when not") {
        weekSynchronizationRepository.markForSynchronization(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week10).shouldBeRight()

        val shifts = weekPlanService.shifts(year2024Week8..year2024Week10)
            .shouldBeRight()

        shifts.flatMap { it.allShifts } shouldContainExactlyInAnyOrder allShiftsInSystem + shiftsNotInSystem
    }

    test("should use shifts repository when synchronized") {
        val shifts = weekPlanService.shifts(year2024Week8..year2024Week10)
            .shouldBeRight()

        shifts.flatMap { it.allShifts } shouldContainExactlyInAnyOrder allShiftsInSystem
    }

    test("should fail when synchronization fails") {
        weekSynchronizationRepository.markForSynchronization(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week10).shouldBeRight()
        salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week10) ShiftsError.NotAuthorized.left() else Unit.right() }

        val error = weekPlanService.shifts(year2024Week8..year2024Week10)
            .shouldBeLeft()

        error shouldBe WeekPlanServiceError.AccessDeniedToSalarySystem
    }

    test("when synchronized, should not bump into salary") {
        salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week9) ShiftsError.NotAuthorized.left() else Unit.right() }
        weekSynchronizationRepository.markSynchronized(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week10).shouldBeRight()

        weekPlanService.shifts(year2024Week8..year2024Week10)
            .shouldBeRight()
    }

    test("should just failure when unable to read shifts repository") {
        shiftRepository.addShiftsErrorRunner {
            if (it == year2024Week8)
                ShiftsError.NotAuthorized.left()
            else
                Unit.right()
        }

        val error = weekPlanService.shifts(year2024Week8..year2024Week10)
            .shouldBeLeft()

        error shouldBe WeekPlanServiceError.CannotCommunicateWithShiftsRepository
    }
})
