package dk.rohdef.helperplanning.shifts

import arrow.core.left
import arrow.core.right
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.shifts.ShiftTestData.Fiktivus
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
    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekSynchronizationRepository = TestWeekSynchronizationRepository()
    val weekPlanService = WeekPlanServiceImplementation(salarySystemRepository, shiftRepository, weekSynchronizationRepository)

    val year2024Week8 = YearWeek(2024, 8)
    val year2024Week9 = YearWeek(2024, 9)
    val year2024Week10 = YearWeek(2024, 10)

    beforeEach {
        salarySystemRepository.reset()
        shiftRepository.reset()
        weekSynchronizationRepository.reset()

        Fiktivus.allShiftsInSystem.forEach { salarySystemRepository.addShift(PrincipalsTestData.FiktivusMaximus.subject, it) }
        Fiktivus.shiftsNotInSystem.forEach { salarySystemRepository.addShift(PrincipalsTestData.FiktivusMaximus.subject, it) }
        Fiktivus.allShiftsInSystem.forEach { shiftRepository.createOrUpdate(PrincipalsTestData.FiktivusMaximus.subject, it) }

        weekSynchronizationRepository.markSynchronized(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week10).shouldBeRight()
    }

    test("should synchronize when not") {
        weekSynchronizationRepository.markForSynchronization(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week10).shouldBeRight()

        val shifts = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week8..year2024Week10)
            .shouldBeRight()

        shifts.flatMap { it.allShifts } shouldContainExactlyInAnyOrder Fiktivus.allShiftsInSystem + Fiktivus.shiftsNotInSystem
    }

    test("should use shifts repository when synchronized") {
        val shifts = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeRight()

        shifts.flatMap { it.allShifts } shouldContainExactlyInAnyOrder Fiktivus.allShiftsInSystem
    }

    test("should fail when synchronization fails") {
        weekSynchronizationRepository.markForSynchronization(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(year2024Week10).shouldBeRight()
        salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week10) ShiftsError.NotAuthorized.left() else Unit.right() }

        val error = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeLeft()

        error shouldBe WeekPlanServiceError.AccessDeniedToSalarySystem
    }

    test("when synchronized, should not bump into salary") {
        salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week9) ShiftsError.NotAuthorized.left() else Unit.right() }
        weekSynchronizationRepository.markSynchronized(year2024Week8).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week9).shouldBeRight()
        weekSynchronizationRepository.markSynchronized(year2024Week10).shouldBeRight()

        weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week8..year2024Week10)
            .shouldBeRight()
    }

    test("should just failure when unable to read shifts repository") {
        shiftRepository.addShiftsErrorRunner {
            if (it == year2024Week8)
                ShiftsError.NotAuthorized.left()
            else
                Unit.right()
        }

        val error = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeLeft()

        error shouldBe WeekPlanServiceError.CannotCommunicateWithShiftsRepository
    }

    context("Principals") {
        test("should reject principal with missing role") {
            val error = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.noRoles, year2024Week8..year2024Week10)
                .shouldBeLeft()

            error shouldBe WeekPlanServiceError.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                listOf(),
            )
        }

        test("should reject principal with wrong role(s)") {
            val error = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.templateAdmin, year2024Week8..year2024Week10)
                .shouldBeLeft()

            error shouldBe WeekPlanServiceError.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                listOf(RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN),
            )
        }

        test("should distinguish between different principals") {
            // set up shift plan for each user

            val fiktivusShifts = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
                .shouldBeRight()
            val realisShifts = weekPlanService.shifts(PrincipalsTestData.RealisMinimalis.shiftAdmin, year2024Week8..year2024Week10)
                .shouldBeRight()


        }
    }
})
