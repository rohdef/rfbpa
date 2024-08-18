package dk.rohdef.helperplanning.shifts

import arrow.core.left
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe

class CreateShiftTest : FunSpec({
    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekSynchronizationRepository = MemoryWeekSynchronizationRepository()
    val weekPlanService =
        WeekPlanServiceImplementation(salarySystemRepository, shiftRepository, weekSynchronizationRepository)

    val shift1Start = YearWeekDayAtTime.parseUnsafe("2024-W13-1T13:30")
    val shift1End = YearWeekDayAtTime.parseUnsafe("2024-W13-1T14:30")

    val all2024Weeks = YearWeek(2024, 1)..YearWeek(2024, 52)
    val week1to12 = YearWeek(2024, 1)..YearWeek(2024, 12)
    val week13 = YearWeek(2024, 13)
    val week14to52 = YearWeek(2024, 14)..YearWeek(2024, 52)

    val testShift1 = Shift(
        HelperBooking.NoBooking,
        generateTestShiftId(shift1Start, shift1End),
        shift1Start,
        shift1End,
    )

    test("Create shift while not synchronized - synchronization state is unchanged") {
        weekPlanService.createShift(PrincipalsTestData.FiktivusMaximus.allRoles, shift1Start, shift1End)

        salarySystemRepository.shiftList shouldContainExactly listOf(testShift1)
        val expectedSynchronizationStates = all2024Weeks.associate {
            it to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE
        }
        weekSynchronizationRepository.synchronizationStates(
            PrincipalsTestData.FiktivusMaximus.subject,
            all2024Weeks,
        ) shouldContainExactly expectedSynchronizationStates
    }

    test("Create shift while not synchronized - synchronization is marked possibly out of date") {
        all2024Weeks.forEach {
            weekSynchronizationRepository.markSynchronized(
                PrincipalsTestData.FiktivusMaximus.subject,
                it
            )
        }
        weekPlanService.createShift(PrincipalsTestData.FiktivusMaximus.allRoles, shift1Start, shift1End)

        salarySystemRepository.shiftList shouldContainExactly listOf(testShift1)

        val expectedSynchronizationWeek1To12 = week1to12.associate {
            it to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }
        val expectedSynchronizationWeek14to52 = week14to52.associate {
            it to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }
        weekSynchronizationRepository.synchronizationStates(
            PrincipalsTestData.FiktivusMaximus.subject,
            week1to12,
        ) shouldContainExactly expectedSynchronizationWeek1To12
        weekSynchronizationRepository.synchronizationState(
            PrincipalsTestData.FiktivusMaximus.subject,
            week13,
        ) shouldBe WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE
        weekSynchronizationRepository.synchronizationStates(
            PrincipalsTestData.FiktivusMaximus.subject,
            week14to52,
        ) shouldContainExactly expectedSynchronizationWeek14to52
    }

    test("Create shift while possibly out of date - synchronization state is unchanged") {
        weekSynchronizationRepository.markPossiblyOutOfDate(PrincipalsTestData.FiktivusMaximus.subject, week13)
        weekPlanService.createShift(PrincipalsTestData.FiktivusMaximus.allRoles, shift1Start, shift1End)

        salarySystemRepository.shiftList shouldContainExactly listOf(testShift1)

        val expectedSynchronizationWeek1To12 = week1to12.associate {
            it to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE
        }
        val expectedSynchronizationWeek14to52 = week14to52.associate {
            it to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE
        }
        weekSynchronizationRepository.synchronizationStates(
            PrincipalsTestData.FiktivusMaximus.subject,
            week1to12,
        ) shouldContainExactly expectedSynchronizationWeek1To12
        weekSynchronizationRepository.synchronizationState(
            PrincipalsTestData.FiktivusMaximus.subject,
            week13,
        ) shouldBe WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE
        weekSynchronizationRepository.synchronizationStates(
            PrincipalsTestData.FiktivusMaximus.subject,
            week14to52,
        ) shouldContainExactly expectedSynchronizationWeek14to52
    }

    test("shift not created in shift repository") {
        shiftRepository.addCreateShiftErrorRunner { ShiftsError.NotAuthorized.left() }
        weekPlanService.createShift(PrincipalsTestData.FiktivusMaximus.allRoles, shift1Start, shift1End)

        salarySystemRepository.shiftList
            .shouldContainExactly(listOf())
    }

    test("Create shift distinguished between subject") {
        TODO()
    }
})
