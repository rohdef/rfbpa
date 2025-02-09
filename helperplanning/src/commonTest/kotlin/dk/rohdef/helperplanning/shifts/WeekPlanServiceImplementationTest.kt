package dk.rohdef.helperplanning.shifts

import arrow.core.left
import arrow.core.nonEmptySetOf
import arrow.core.right
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.helpers.HelperTestData
import dk.rohdef.helperplanning.shifts.ShiftTestData.Fiktivus
import dk.rohdef.rfweeks.YearWeek
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class WeekPlanServiceImplementationTest : FunSpec({
    val salarySystemRepository = TestSalarySystemRepository()
    val shiftRepository = TestShiftRespository()
    val weekSynchronizationRepository = TestWeekSynchronizationRepository()
    val weekPlanService =
        WeekPlanServiceImplementation(salarySystemRepository, shiftRepository, weekSynchronizationRepository)

    val year2024Week8 = YearWeek(2024, 8)
    val year2024Week9 = YearWeek(2024, 9)
    val year2024Week10 = YearWeek(2024, 10)

    beforeEach {
        salarySystemRepository.reset()
        shiftRepository.reset()
        weekSynchronizationRepository.reset()

        Fiktivus.allShiftsInSystem.forEach {
            salarySystemRepository.addShift(
                PrincipalsTestData.FiktivusMaximus.subject,
                it
            )
        }
        Fiktivus.shiftsNotInSystem.forEach {
            salarySystemRepository.addShift(
                PrincipalsTestData.FiktivusMaximus.subject,
                it
            )
        }
        Fiktivus.allShiftsInSystem.forEach {
            shiftRepository.createOrUpdate(
                PrincipalsTestData.FiktivusMaximus.subject,
                it
            )
        }

        weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
            .shouldBeRight()
        weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
            .shouldBeRight()
        weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
            .shouldBeRight()
    }

    test("should synchronize when not") {
        weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
            .shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
            .shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
            .shouldBeRight()

        val shifts =
            weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week8..year2024Week10)
                .shouldBeRight()

        shifts.flatMap { it.allShifts } shouldContainExactlyInAnyOrder Fiktivus.allShiftsInSystem + Fiktivus.shiftsNotInSystem
    }

    test("should use shifts repository when synchronized") {
        val shifts = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeRight()

        shifts.flatMap { it.allShifts } shouldContainExactlyInAnyOrder Fiktivus.allShiftsInSystem
    }

    test("should fail when synchronization fails") {
        weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
            .shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
            .shouldBeRight()
        weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
            .shouldBeRight()
        salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week10) ShiftsError.NotAuthorized.left() else Unit.right() }

        val error = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeLeft()

        error shouldBe WeekPlanServiceError.AccessDeniedToSalarySystem
    }

    test("when synchronized, should not bump into salary") {
        salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week9) ShiftsError.NotAuthorized.left() else Unit.right() }
        weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
            .shouldBeRight()
        weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
            .shouldBeRight()
        weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
            .shouldBeRight()

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

    context("Helper illness") {
        context("reporting") {
            salarySystemRepository.idGenerator = TestSalarySystemRepository.IdGenerator.Random
            test("should add illness registration") {
                val shift: Shift = Fiktivus.week10Shift1
                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )

                val replacementShift = illnessReportResult.shouldBeRight()
                val replacementShiftId = replacementShift.shiftId

                val expectedShift = shift.copy(
                    registrations = listOf(Registration.Illness(replacementShiftId))
                )
                shiftRepository.shifts[shift.shiftId] shouldBe expectedShift
                salarySystemRepository.shifts[shift.shiftId] shouldBe expectedShift
            }

            test("should create new shift") {
                val shift = Fiktivus.week10Shift1
                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )

                val replacementShift = illnessReportResult.shouldBeRight()
                val replacementShiftId = replacementShift.shiftId

                val expectedShift = shift.copy(
                    shiftId = replacementShiftId,
                    helperBooking = HelperBooking.NoBooking,
                )
                shiftRepository.shifts[replacementShiftId] shouldBe expectedShift
                salarySystemRepository.shifts[replacementShiftId] shouldBe expectedShift
            }

            test("should only be possible on a booked shift") {
                val shift = Fiktivus.week9Shift1

                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMustBeBooked(shift.shiftId)
            }

            test("should do 'nothing' and give same ID if registration is already present") {
                val shift = Fiktivus.week10Shift1

                val illnessReportResult1 = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )
                val newShift1 = illnessReportResult1.shouldBeRight()
                val illnessReportResult2 = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )
                val newShift2 = illnessReportResult2.shouldBeRight()

                val expectedShift = shift.copy(
                    registrations = listOf(Registration.Illness(newShift1.shiftId))
                )
                val expectedNewShift = shift.copy(
                    shiftId = newShift1.shiftId,
                    helperBooking = HelperBooking.NoBooking,
                )
                shiftRepository.shifts.values
                    .filter { it.start == shift.start }
                    .filter { it.end == shift.end }
                    .shouldContainExactlyInAnyOrder(expectedNewShift, expectedShift)
                salarySystemRepository.shifts.values
                    .filter { it.start == shift.start }
                    .filter { it.end == shift.end }
                    .shouldContainExactlyInAnyOrder(expectedNewShift, expectedShift)
                newShift1 shouldBeEqual newShift2
            }

            test("should fail if shift isn't found in salary system") {
                val shift = Fiktivus.week10Shift1
                salarySystemRepository.removeShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    shift.shiftId,
                )

                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMissingInSalarySystem(shift.shiftId)
            }

            test("should fail if shift isn't found in repository") {
                val shift = Fiktivus.week10ShiftNotInSystem

                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMissingInShiftSystem(shift.shiftId)
            }

            test("should mark week as out of sync there if an error occurs") {
                val shift = Fiktivus.week10Shift1
                salarySystemRepository.removeShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    shift.shiftId,
                )

                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )

                illnessReportResult.shouldBeLeft()

                weekSynchronizationRepository.synchronizationState(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
                    .shouldBe(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)
            }

            test("should mark week as possibly out of sync if successful") {
                val shift = Fiktivus.week10Shift1
                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shift.shiftId,
                )

                illnessReportResult.shouldBeRight()

                weekSynchronizationRepository.synchronizationState(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
                    .shouldBe(WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE)
            }

            xtest("should delete replacement shift if illness registration fails") {}

            xtest("should give explicit error if it cannot delete replacement shift when illness registration fails") {}
        }
    }

    context("Helper bookings") {
        test("booking a helper") {
            val shift2 = Fiktivus.week8Shift2

            val booking = weekPlanService.changeHelperBooking(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                shift2.shiftId,
                HelperBooking.Booked(HelperTestData.permanentHipHop.id),
            )

            val expectedShift = Shift(
                HelperBooking.Booked(HelperTestData.permanentHipHop.id),
                shift2.shiftId,
                shift2.start,
                shift2.end,
            )

            booking.shouldBeRight()
            shiftRepository.shifts[shift2.shiftId] shouldBe expectedShift
            salarySystemRepository.shifts[shift2.shiftId] shouldBe expectedShift
        }

        test("unbooking a helper") {
            val shift2 = Fiktivus.week10Shift2

            val booking = weekPlanService.changeHelperBooking(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                shift2.shiftId,
                HelperBooking.NoBooking,
            )

            val expectedShift = Shift(
                HelperBooking.NoBooking,
                shift2.shiftId,
                shift2.start,
                shift2.end,
            )

            booking.shouldBeRight()
            shiftRepository.shifts[shift2.shiftId] shouldBe expectedShift
            salarySystemRepository.shifts[shift2.shiftId] shouldBe expectedShift
        }

        xcontext("Error cases") {
            xtest("vacancy booking") {
                // TODO: 22/10/2024 rohdef - should this somehow be more explicit? - logic must be metadata only
                TODO()
            }

            xtest("helper not known by AXP") {
            }
        }
    }

    context("Principals") {
        test("should reject principal with wrong role(s)") {
            val error =
                weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.helperAdmin, year2024Week8..year2024Week10)
                    .shouldBeLeft()

            error shouldBe WeekPlanServiceError.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.HELPER_ADMIN),
            )
        }

        test("should distinguish between different principals") {
            // TODO set up shift plan for each user

            val fiktivusShifts =
                weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
                    .shouldBeRight()
            val realisShifts =
                weekPlanService.shifts(PrincipalsTestData.RealisMinimalis.shiftAdmin, year2024Week8..year2024Week10)
                    .shouldBeRight()


        }
    }
})
