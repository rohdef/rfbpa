package dk.rohdef.helperplanning.shifts

import arrow.core.left
import arrow.core.right
import dk.rohdef.helperplanning.PrincipalsTestData
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.helperplanning.helpers.HelperTestData
import dk.rohdef.helperplanning.salary_shifts.SalaryBooking
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.helperplanning.shifts.yaml.Shifties
import dk.rohdef.rfweeks.YearWeek
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import net.mamoe.yamlkt.Yaml

class WeekPlanServiceImplementationTest : FunSpec({
    val schedule = WeekPlanServiceImplementationTest::class.java.classLoader
        .getResource("shifts/synchronization-schedules.yaml")!!.readText()
    val shifties = Yaml.decodeFromString(Shifties.serializer(), schedule)

    lateinit var dataHelper: DataHelper

    lateinit var weekPlanService: WeekPlanService

    val year2024Week8 = YearWeek(2024, 8)
    val year2024Week9 = YearWeek(2024, 9)
    val year2024Week10 = YearWeek(2024, 10)

    beforeEach {
        dataHelper = DataHelper
            .create(shifties, PrincipalsTestData.FiktivusMaximus.subject)
            .shouldBeRight()

        weekPlanService = WeekPlanServiceImplementation(
            dataHelper.salarySystem,
            dataHelper.shiftRepository,
            dataHelper.helpers.helpers,
            dataHelper.weekSynchronizationRepository,
        )
    }

    test("should synchronize when not") {
        dataHelper.weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
        dataHelper.weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
        dataHelper.weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)

        val shifts = weekPlanService
                .shifts(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week8..year2024Week10)
                .shouldBeRight()
        val allShifts = shifts.flatMap { it.allShifts }

        val expectedShifts =
            shifties.rfbpaShifts(dataHelper.allHelpersByShortName) +
                    shifties.rfbpaShiftsMissing(dataHelper.allHelpersByShortName, dataHelper.helperByShiftId)
        allShifts
            .shouldContainExactlyInAnyOrder(expectedShifts)
    }

    test("should use shifts repository when synchronized") {
        dataHelper.weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
        dataHelper.weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
        dataHelper.weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)

        val shifts = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeRight()
        val allShifts = shifts.flatMap { it.allShifts }

        allShifts shouldContainExactlyInAnyOrder shifties.rfbpaShifts(dataHelper.allHelpersByShortName)
    }

    test("should fail when synchronization fails") {
        dataHelper.weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
            .shouldBeRight()
        dataHelper.weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
            .shouldBeRight()
        dataHelper.weekSynchronizationRepository.markForSynchronization(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
            .shouldBeRight()
        dataHelper.salarySystem.addShiftsErrorRunner { if (it == year2024Week10) ShiftsError.NotAuthorized.left() else Unit.right() }

        val error = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeLeft()

        error shouldBe WeekPlanServiceError.AccessDeniedToSalarySystem
    }

    test("when synchronized, should not bump into salary") {
        dataHelper.salarySystem.addShiftsErrorRunner { if (it == year2024Week9) ShiftsError.NotAuthorized.left() else Unit.right() }
        dataHelper.weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week8)
            .shouldBeRight()
        dataHelper.weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week9)
            .shouldBeRight()
        dataHelper.weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week10)
            .shouldBeRight()

        weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week8..year2024Week10)
            .shouldBeRight()
    }

    test("should just failure when unable to read shifts repository") {
        dataHelper.shiftRepository.addShiftsErrorRunner {
            if (it == year2024Week8)
                ShiftsError.NotAuthorized.left()
            else
                Unit.right()
        }

        val error = weekPlanService.shifts(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week8..year2024Week10)
            .shouldBeLeft()

        error shouldBe WeekPlanServiceError.CannotCommunicateWithShiftsRepository
    }

    xcontext("helper illness") {
        context("reporting") {
            dataHelper.salarySystem.idGenerator = TestSalarySystemRepository.IdGenerator.Random

            val allShifts = shifties.rfbpaShifts(dataHelper.allHelpersByShortName)
            val week9Shift1 = allShifts.filter { it.start.yearWeek == YearWeek(2024, 9) }
                .get(0)
            val week10Shift1 = allShifts.filter { it.start.yearWeek == YearWeek(2024, 10) }
                .get(0)

            test("should add illness registration") {
                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                val replacementShift = illnessReportResult.shouldBeRight()
                val replacementShiftId = replacementShift.shiftId

                val expectedShift = week10Shift1.copy(
                    registrations = listOf(Registration.Illness(replacementShiftId))
                )
                dataHelper.shiftRepository.shifts[week10Shift1.shiftId] shouldBe expectedShift
                dataHelper.salarySystem.shifts[week10Shift1.shiftId] shouldBe expectedShift
            }

            test("should create new shift") {
                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                val replacementShift = illnessReportResult.shouldBeRight()
                val replacementShiftId = replacementShift.shiftId

                val expectedShift = week10Shift1.copy(
                    shiftId = replacementShiftId,
                    helperBooking = HelperBooking.NoBooking,
                )
                dataHelper.shiftRepository.shifts[replacementShiftId] shouldBe expectedShift
                dataHelper.salarySystem.shifts[replacementShiftId] shouldBe expectedShift
            }

            test("should only be possible on a booked shift") {
                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week9Shift1.shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMustBeBooked(week9Shift1.shiftId)
            }

            test("should do 'nothing' and give same ID if registration is already present") {
                val illnessReportResult1 = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )
                val newShift1 = illnessReportResult1.shouldBeRight()
                val illnessReportResult2 = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )
                val newShift2 = illnessReportResult2.shouldBeRight()

                val expectedShift = week10Shift1.copy(
                    registrations = listOf(Registration.Illness(newShift1.shiftId))
                )
                val expectedNewShift = week10Shift1.copy(
                    shiftId = newShift1.shiftId,
                    helperBooking = HelperBooking.NoBooking,
                )
                dataHelper.shiftRepository.shifts.values
                    .filter { it.start == week10Shift1.start }
                    .filter { it.end == week10Shift1.end }
                    .shouldContainExactlyInAnyOrder(expectedNewShift, expectedShift)
                dataHelper.salarySystem.shifts.values
                    .filter { it.start == week10Shift1.start }
                    .filter { it.end == week10Shift1.end }
                    .shouldContainExactlyInAnyOrder(expectedNewShift, expectedShift)
                newShift1 shouldBeEqual newShift2
            }

            test("should fail if shift isn't found in salary system") {
                dataHelper.salarySystem.removeShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    week10Shift1.shiftId,
                )

                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMissingInSalarySystem(week10Shift1.shiftId)
            }

            test("should fail if shift isn't found in repository") {
                val shiftId = ShiftId.generateId()

                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMissingInShiftSystem(shiftId)
            }

            test("should mark week as out of sync there if an error occurs") {
                dataHelper.salarySystem.removeShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    week10Shift1.shiftId,
                )

                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                illnessReportResult.shouldBeLeft()

                dataHelper.weekSynchronizationRepository.synchronizationState(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week10
                )
                    .shouldBe(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)
            }

            test("should mark week as possibly out of sync if successful") {
                val illnessReportResult = weekPlanService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                illnessReportResult.shouldBeRight()

                dataHelper.weekSynchronizationRepository.synchronizationState(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week10
                )
                    .shouldBe(WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE)
            }

            xtest("should delete replacement shift if illness registration fails") {}

            xtest("should give explicit error if it cannot delete replacement shift when illness registration fails") {}
        }
    }

    context("helper bookings") {
        val allShifts = shifties.rfbpaShifts(dataHelper.allHelpersByShortName)
        val week8shift2 = allShifts.filter { it.start.yearWeek == YearWeek(2024, 8) }
            .get(1)
        val week10Shift2 = allShifts.filter { it.start.yearWeek == YearWeek(2024, 10) }
            .get(1)

        xtest("Helper bookings ") {
            TODO("Tests needed for more full helper bookings logic")
        }

        test("booking a helper") {
            val booking = weekPlanService.bookHelper(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                week8shift2.shiftId,
                HelperTestData.permanentHipHop.id,
            )

            val expectedShift = Shift(
                HelperBooking.Booked(HelperTestData.permanentHipHop.id),
                week8shift2.shiftId,
                week8shift2.start,
                week8shift2.end,
            )
            val expectedSalaryShift = SalaryShift(
                SalaryBooking.Helper(HelperTestData.permanentHipHop.id),
                week8shift2.shiftId,
                week8shift2.start,
                week8shift2.end,
            )

            booking.shouldBeRight()
            dataHelper.shiftRepository.shifts[week8shift2.shiftId] shouldBe expectedShift
            dataHelper.salarySystem.shifts[week8shift2.shiftId] shouldBe expectedSalaryShift
        }

        test("unbooking a helper") {
            val booking = weekPlanService.unbookHelper(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                week10Shift2.shiftId,
            )

            val expectedShift = Shift(
                HelperBooking.NoBooking,
                week10Shift2.shiftId,
                week10Shift2.start,
                week10Shift2.end,
            )
            val expectedSalaryShift = SalaryShift(
                SalaryBooking.NoBooking,
                week10Shift2.shiftId,
                week10Shift2.start,
                week10Shift2.end,
            )

            booking.shouldBeRight()
            dataHelper.shiftRepository.shifts[week10Shift2.shiftId] shouldBe expectedShift
            dataHelper.salarySystem.shifts[week10Shift2.shiftId] shouldBe expectedSalaryShift
        }

        context("from shift listing") {
            context("for a new shift") {
                test("booked to a known helper") {}

                test("booked to an unknown helper") {
                    // create in salary

                    // list/sync

                    // confirm empty shift-helper created
                }

                test("not booked") {
                }
            }

            context("for an existing shift") {
                test("the helper is unbooked") {
                    // add known helper by salary
                    // add known helper by rfbpa
                    // add already unbooked

                    // sync

                    // check all three unbooked
                }

                test("helper is known by salary") {
                    // add known different helper by salary
                    // add known same helper by salary
                    // add known helper by rfbpa
                    // add unbooked

                    // sync

                    // all match salary
                }

                test("helper unknown in salaray known by rfbpa (by shift)") {
                    // add known helper by salary
                    // add known by rfbpa
                    // add unbooked

                    // sync

                    // all match rfbpa
                }
            }
        }

        context("Error cases") {
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
                PrincipalsTestData.FiktivusMaximus.helperAdmin,
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
            )
        }

        xtest("should distinguish between different principals") {
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
