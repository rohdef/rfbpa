package dk.rohdef.helperplanning.shifts

import arrow.core.none
import dk.rohdef.helperplanning.PrincipalsTestData
import dk.rohdef.helperplanning.RfbpaTime
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.salary_shifts.SalaryBooking
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.helperplanning.shifts.yaml.Shifties
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import net.mamoe.yamlkt.Yaml

class ShiftsServiceImplementationTest : FunSpec({
    val schedule = WeekPlanServiceImplementationTest::class.java.classLoader
        .getResource("shifts/synchronization-schedules.yaml")!!.readText()
    val shifties = Yaml.decodeFromString(Shifties.serializer(), schedule)
    val time = RfbpaTime(FixedClock())

    lateinit var dataHelper: DataHelper

    lateinit var shiftService: ShiftsServiceImplementation
    lateinit var weekPlanService: WeekPlanServiceImplementation

    val year2024Week8 = YearWeek(2024, 8)
    val year2024Week9 = YearWeek(2024, 9)
    val year2024Week10 = YearWeek(2024, 10)

    beforeTest {
        dataHelper = DataHelper
            .create(shifties, PrincipalsTestData.FiktivusMaximus.subject)
            .shouldBeRight()

        shiftService = ShiftsServiceImplementation(
            dataHelper.salarySystem,
            dataHelper.shiftRepository,
            dataHelper.helpers.helpers,
            dataHelper.weekSynchronizationRepository,
            time
        )

        weekPlanService = WeekPlanServiceImplementation(
            dataHelper.salarySystem,
            dataHelper.shiftRepository,
            dataHelper.helpers.helpers,
            dataHelper.weekSynchronizationRepository
        )
    }

    context("helper illness") {
        // TODO test synchronization
        context("reading") {
            test("helper manager does not have replacement shift") {
                // Week 2026-W18 - 27-04-2026 -- 03-05-2026
                val expectedRegistrationTime = LocalDateTime.parse("2026-04-27T19:41:17+01:00")
//                time.clock.fixedInstant = expectedRegistrationTime.toInstant(TimeZone.UTC)

                val shiftId = ShiftId.generateId()
                dataHelper.salarySystem.addShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    SalaryShift(
                        SalaryBooking.Helper(HelperId.generateId()),
                        shiftId,
                        YearWeekDayAtTime.parseUnsafe("2026-W18-3T11:30"),
                        YearWeekDayAtTime.parseUnsafe("2026-W18-3T22:15"),
                        listOf(
                            Registration.Illness(expectedRegistrationTime, ShiftId.generateId()),
                        ),
                    ),
                )

                // sync needed, can we ensure ID? Yes, That is most likely same as above
                weekPlanService.synchronize(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    YearWeek(2026, 18)
                )

                val shift = shiftService.shiftById(PrincipalsTestData.FiktivusMaximus.allRoles, shiftId)
                    .shouldBeRight()

                 shift.registrations shouldContainExactly listOf(
                     Registration.Illness(expectedRegistrationTime, none())
                 )
            }
        }

        context("reporting") {
            fun HelperBooking.toSalaryBooking(): SalaryBooking {
                return when (this) {
                    is HelperBooking.Booked -> SalaryBooking.Helper(this.helper)
                    HelperBooking.NoBooking -> SalaryBooking.NoBooking
                }
            }

            fun Shift.toSalaryShift(): SalaryShift {
                return SalaryShift(
                    helperBooking.toSalaryBooking(),
                    shiftId,
                    start,
                    end,
                    registrations,
                )
            }

            dataHelper.salarySystem.idGenerator = TestSalarySystemRepository.IdGenerator.Random

            val allShifts = shifties.rfbpaShifts(dataHelper.allHelpersByShortName)
            val week9Shift1 = allShifts.filter { it.start.yearWeek == YearWeek(2024, 9) }
                .get(0)
            val week10Shift1 = allShifts.filter { it.start.yearWeek == YearWeek(2024, 10) }
                .get(0)

            test("should add illness registration") {
                val illnessReportResult = shiftService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                val replacementShift = illnessReportResult.shouldBeRight()
                val replacementShiftId = replacementShift.shiftId

                val expectedShift = week10Shift1.copy(
                    registrations = listOf(
                        Registration.Illness(time.localDateTime(), replacementShiftId),
                    )
                )
                dataHelper.shiftRepository.shifts[week10Shift1.shiftId] shouldBe expectedShift
                dataHelper.salarySystem.shifts[week10Shift1.shiftId] shouldBe expectedShift.toSalaryShift()
            }

            test("should create new shift") {
                val illnessReportResult = shiftService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                val replacementShift = illnessReportResult.shouldBeRight()
                val replacementShiftId = replacementShift.shiftId

                val expectedShift = week10Shift1.copy(
                    shiftId = replacementShiftId,
                    helperBooking = HelperBooking.NoBooking,
                    registrations = listOf(
                        Registration.IllnessReplacement(week10Shift1.shiftId),
                    )
                )
                dataHelper.shiftRepository.shifts[replacementShiftId] shouldBe expectedShift
                dataHelper.salarySystem.shifts[replacementShiftId] shouldBe expectedShift.toSalaryShift()
            }

            test("should only be possible on a booked shift") {
                val illnessReportResult = shiftService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week9Shift1.shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMustBeBooked(week9Shift1.shiftId)
            }

            test("should do 'nothing' and give same ID if registration is already present") {
                val illnessReportResult1 = shiftService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )
                val newShift1 = illnessReportResult1.shouldBeRight()
                val illnessReportResult2 = shiftService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )
                val newShift2 = illnessReportResult2.shouldBeRight()

                val expectedShift = week10Shift1.copy(
                    registrations = listOf(Registration.Illness(time.localDateTime(), newShift1.shiftId))
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
                    .shouldContainExactlyInAnyOrder(expectedNewShift.toSalaryShift(), expectedShift.toSalaryShift())
                newShift1 shouldBeEqual newShift2
            }

            test("should fail if shift isn't found in salary system") {
                dataHelper.salarySystem.removeShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    week10Shift1.shiftId,
                )

                val illnessReportResult = shiftService.reportIllness(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    week10Shift1.shiftId,
                )

                val error = illnessReportResult.shouldBeLeft()

                error shouldBe WeekPlanServiceError.ShiftMissingInSalarySystem(week10Shift1.shiftId)
            }

            test("should fail if shift isn't found in repository") {
                val shiftId = ShiftId.generateId()

                val illnessReportResult = shiftService.reportIllness(
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

                val illnessReportResult = shiftService.reportIllness(
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
                val illnessReportResult = shiftService.reportIllness(
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
})