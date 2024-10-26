package dk.rohdef.helperplanning.shifts

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.templates.TemplateTestData.generateTestShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek

class SynchronizationTest : FunSpec({
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
    val weekPlanService =
        WeekPlanServiceImplementation(salarySystemRepository, shiftRepository, weekSynchronizationRepository)

    val year2024Week13 = YearWeek(2024, 13)

    val week13Shift1 = year2024Week13.shift(DayOfWeek.MONDAY).start(13, 30).end(14, 30)
    val week13Shift2 = year2024Week13.shift(DayOfWeek.WEDNESDAY).start(17, 30).end(21, 30)
    val week13Shift3 = year2024Week13.shift(DayOfWeek.THURSDAY).start(9, 45).end(15, 15)
    val week13Shifts = listOf(
        week13Shift1,
        week13Shift2,
        week13Shift3,
    )

    val shiftNotInSystem = year2024Week13.shift(DayOfWeek.SATURDAY).start(8, 0).end(15, 45)

    val year2024Week14 = YearWeek(2024, 14)
    val year2024Week15 = YearWeek(2024, 15)
    val year2024Week16 = YearWeek(2024, 16)

    val shift9Start = year2024Week16.atDayOfWeek(DayOfWeek.WEDNESDAY).atTime(9, 45)
    val shift9End = year2024Week16.atDayOfWeek(DayOfWeek.FRIDAY).atTime(23, 45)
    val shift4 = year2024Week14.shift(DayOfWeek.MONDAY).start(13, 30).end(14, 30)
    val shift5 = year2024Week14.shift(DayOfWeek.MONDAY).start(19, 0).end(20, 45)
    val shift6 = year2024Week14.shift(DayOfWeek.WEDNESDAY).start(8, 15).end(21, 45)
    val shift7 = year2024Week15.shift(DayOfWeek.MONDAY).start(11, 15).end(15, 30)
    val shift8 = year2024Week15.shift(DayOfWeek.THURSDAY).start(5, 45).end(9, 45)
    val shift9 = createTestShift(shift9Start, shift9End)
    val shift10 = year2024Week16.shift(DayOfWeek.SATURDAY).start(8, 15).end(10, 30)
    val shift11 = year2024Week16.shift(DayOfWeek.SATURDAY).start(16, 0).end(21, 15)
    val shift12 = year2024Week16.shift(DayOfWeek.SATURDAY).start(9, 0).end(22, 0)
    val week14Shifts = listOf(shift4, shift5, shift6)
    val week15Shifts = listOf(shift7, shift8)
    val week16Shifts = listOf(shift9, shift10, shift11, shift12)
    val additionalShifts = week14Shifts + week15Shifts + week16Shifts

    val shiftsToAdd = (week13Shifts + additionalShifts).map { it.start.yearWeek }
        .distinct()
        .map { it.atDayOfWeek(DayOfWeek.TUESDAY) }
        .map { createTestShift(it.atTime(4, 0), it.atTime(12, 0)) }

    beforeEach {
        weekSynchronizationRepository.reset()
        shiftRepository.reset()

        salarySystemRepository.apply {
            reset()

            createShift(PrincipalsTestData.FiktivusMaximus.subject, week13Shift1.start, week13Shift1.end)
            createShift(PrincipalsTestData.FiktivusMaximus.subject, week13Shift2.start, week13Shift2.end)
            createShift(PrincipalsTestData.FiktivusMaximus.subject, week13Shift3.start, week13Shift3.end)
        }
    }

    context("Single week") {
        test("Not syncronized") {
            shiftRepository.shiftList.shouldBeEmpty()
            weekSynchronizationRepository.synchronizationState(
                PrincipalsTestData.FiktivusMaximus.subject,
                year2024Week13,
            ) shouldBe WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE

            weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week13)
                .shouldBeRight()

            shiftRepository.shiftList shouldContainExactlyInAnyOrder week13Shifts
            weekSynchronizationRepository.synchronizationState(
                PrincipalsTestData.FiktivusMaximus.subject,
                year2024Week13,
            ) shouldBe WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }

        test("Aleady synchronized - synchronization only happens when requested") {
            weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week13)
            salarySystemRepository.addShift(PrincipalsTestData.FiktivusMaximus.subject, shiftNotInSystem)

            weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week13)
                .shouldBeRight()

            shiftRepository.shiftList shouldContainExactlyInAnyOrder week13Shifts
            weekSynchronizationRepository.synchronizationState(
                PrincipalsTestData.FiktivusMaximus.subject,
                year2024Week13,
            ) shouldBe WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }

        test("Synchronized but marked for synchronization") {
            weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week13)
            weekSynchronizationRepository.markForSynchronization(
                PrincipalsTestData.FiktivusMaximus.subject,
                year2024Week13,
            )
            salarySystemRepository.addShift(PrincipalsTestData.FiktivusMaximus.subject, shiftNotInSystem)

            weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week13)
                .shouldBeRight()

            shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + shiftNotInSystem)
            weekSynchronizationRepository.synchronizationState(
                PrincipalsTestData.FiktivusMaximus.subject,
                year2024Week13,
            ) shouldBe WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED
        }

        test("Create shift marks for synchhronization") {
            val targetYearWeek = shiftNotInSystem.start.yearWeek
            weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, targetYearWeek)
            salarySystemRepository.addShift(PrincipalsTestData.FiktivusMaximus.subject, shiftNotInSystem)

            weekPlanService.createShift(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                shiftNotInSystem.start,
                shiftNotInSystem.end,
            )
                .shouldBeRight()

            salarySystemRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + shiftNotInSystem)
            weekSynchronizationRepository.synchronizationState(
                PrincipalsTestData.FiktivusMaximus.subject,
                targetYearWeek,
            ) shouldBe WeekSynchronizationRepository.SynchronizationState.POSSIBLY_OUT_OF_DATE
        }
    }

    context("Multiple weeks") {
        beforeEach {
            additionalShifts.forEach {
                salarySystemRepository.createShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    it.start,
                    it.end,
                )
            }
        }

        test("week interval") {
            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week13..year2024Week16
                ).values
            synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)

            weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.shiftAdmin, year2024Week13..year2024Week16)
                .shouldBeRight()

            val synchronizationStatesSync =
                weekSynchronizationRepository.synchronizationStates(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week13..year2024Week16
                ).values
            synchronizationStatesSync shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED)

            shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + additionalShifts)
        }

        test("weeks in interval with interruptions") {
            // TODO make to test factory repeater thing
            weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week14)
            weekSynchronizationRepository.markSynchronized(PrincipalsTestData.FiktivusMaximus.subject, year2024Week15)

            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week13..year2024Week16
                )
            synchronizationStates shouldContainExactly mapOf(
                year2024Week13 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                year2024Week14 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week15 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                year2024Week16 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
            )

            shiftsToAdd.forEach { salarySystemRepository.addShift(PrincipalsTestData.FiktivusMaximus.subject, it) }

            weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week13..year2024Week16)
                .shouldBeRight()

            val synchronizationStatesSync =
                weekSynchronizationRepository.synchronizationStates(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week13..year2024Week16
                ).values
            synchronizationStatesSync shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED)

            val tuesdays = shiftRepository.shiftList
                .filter { it.start.dayOfWeek == DayOfWeek.TUESDAY }
                .map { it.start.yearWeek }
            tuesdays shouldContainExactlyInAnyOrder listOf(year2024Week13, year2024Week16)
        }
    }

    context("Error scenarios") {
        beforeEach {
            additionalShifts.forEach {
                salarySystemRepository.createShift(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    it.start,
                    it.end,
                )
            }
        }

        context("Salary system") {
            test("Fail as domain failure") {
                val synchronizationStates =
                    weekSynchronizationRepository.synchronizationStates(
                        PrincipalsTestData.FiktivusMaximus.subject,
                        year2024Week13..year2024Week16,
                    ).values
                synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)
                salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week13) ShiftsError.NotAuthorized.left() else Unit.right() }

                val errors = weekPlanService.synchronize(
                    PrincipalsTestData.FiktivusMaximus.shiftAdmin,
                    year2024Week13..year2024Week16,
                )
                    .shouldBeLeft()

                val updatedSynchronizationStates =
                    weekSynchronizationRepository.synchronizationStates(
                        PrincipalsTestData.FiktivusMaximus.subject,
                        year2024Week13..year2024Week16,
                    )
                updatedSynchronizationStates shouldContainExactly mapOf(
                    year2024Week13 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                    year2024Week14 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week15 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week16 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                )
                shiftRepository.shiftList shouldContainExactlyInAnyOrder (additionalShifts)

                errors shouldContainExactlyInAnyOrder listOf(
                    SynchronizationError.CouldNotSynchronizeWeek(year2024Week13),
                )
            }
        }

        context("Shifts repository") {
            test("Fail as domain failure") {
                shiftRepository.addCreateShiftErrorRunner { if (it == shift11) ShiftsError.NotAuthorized.left() else Unit.right() }
                val synchronizationStates =
                    weekSynchronizationRepository.synchronizationStates(
                        PrincipalsTestData.FiktivusMaximus.subject,
                        year2024Week13..year2024Week16
                    ).values
                synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)

                val errors = weekPlanService.synchronize(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    year2024Week13..year2024Week16,
                )
                    .shouldBeLeft()

                val updatedSynchronizationStates =
                    weekSynchronizationRepository.synchronizationStates(
                        PrincipalsTestData.FiktivusMaximus.subject,
                        year2024Week13..year2024Week16
                    )
                updatedSynchronizationStates shouldContainExactly mapOf(
                    year2024Week13 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week14 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week15 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week16 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                )
                shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + additionalShifts - shift11)

                errors shouldContainExactlyInAnyOrder listOf(
                    SynchronizationError.CouldNotSynchronizeWeek(year2024Week16),
                )
            }
        }

        context("Synchronization repository") {
            test("Fail as domain failure") {
                weekSynchronizationRepository.addMarkSynchronizedPreRunners {
                    if (it == year2024Week16) WeekSynchronizationRepository.CannotChangeSyncronizationState(
                        PrincipalsTestData.FiktivusMaximus.subject,
                        year2024Week13,
                    ).left() else Unit.right()
                }
                val synchronizationStates =
                    weekSynchronizationRepository.synchronizationStates(
                        PrincipalsTestData.FiktivusMaximus.subject,
                        year2024Week13..year2024Week16
                    ).values
                synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)

                val errors = weekPlanService.synchronize(
                    PrincipalsTestData.FiktivusMaximus.allRoles,
                    year2024Week13..year2024Week16,
                )
                    .shouldBeLeft()

                val updatedSynchronizationStates =
                    weekSynchronizationRepository.synchronizationStates(
                        PrincipalsTestData.FiktivusMaximus.subject,
                        year2024Week13..year2024Week16
                    )
                updatedSynchronizationStates shouldContainExactly mapOf(
                    year2024Week13 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week14 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week15 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
                    year2024Week16 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                )
                shiftRepository.shiftList shouldContainExactlyInAnyOrder (week13Shifts + additionalShifts)

                errors shouldContainExactlyInAnyOrder listOf(
                    SynchronizationError.CouldNotSynchronizeWeek(year2024Week16),
                )
            }
        }

        test("Multiple errors in combination") {
            weekSynchronizationRepository.addMarkSynchronizedPreRunners {
                if (it == year2024Week14) WeekSynchronizationRepository.CannotChangeSyncronizationState(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week13,
                ).left()
                else Unit.right()
            }
            shiftRepository.addCreateShiftErrorRunner { if (it == week13Shift1) ShiftsError.NotAuthorized.left() else Unit.right() }
            salarySystemRepository.addShiftsErrorRunner { if (it == year2024Week15) ShiftsError.NotAuthorized.left() else Unit.right() }
            val synchronizationStates =
                weekSynchronizationRepository.synchronizationStates(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week13..year2024Week16,
                ).values
            synchronizationStates shouldContainOnly listOf(WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE)

            val errors =
                weekPlanService.synchronize(PrincipalsTestData.FiktivusMaximus.allRoles, year2024Week13..year2024Week16)
                    .shouldBeLeft()

            val updatedSynchronizationStates =
                weekSynchronizationRepository.synchronizationStates(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    year2024Week13..year2024Week16,
                )
            updatedSynchronizationStates shouldContainExactly mapOf(
                year2024Week13 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                year2024Week14 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                year2024Week15 to WeekSynchronizationRepository.SynchronizationState.OUT_OF_DATE,
                year2024Week16 to WeekSynchronizationRepository.SynchronizationState.SYNCHRONIZED,
            )
            shiftRepository.shiftList shouldContainExactlyInAnyOrder ((week13Shifts - week13Shift1) + week14Shifts + week16Shifts)

            errors shouldContainExactlyInAnyOrder listOf(
                SynchronizationError.CouldNotSynchronizeWeek(year2024Week13),
                SynchronizationError.CouldNotSynchronizeWeek(year2024Week14),
                SynchronizationError.CouldNotSynchronizeWeek(year2024Week15),
            )
        }
    }

    xcontext("Mismatch scenarios") {
        // Shift missing in salary repository
        // - check on the salary ID (could have been moved to different week, yuck)
        // - if really not existing, remove
        // - mark synced

        // shift missing in shifts repository
        // - just add
        // - mark synced

        // shift already exists, but different
        // - salary rules, update shift repo
        // - mark synced

        // booking changed (is this part of it? Yes I think so! Scenario for when booking sync is added)
        // - salary rules, update booking repo
        // - mark synced

        // consideration, does illness affect this
        // consideration, what about supplements (tillæg)
        //      supplements might have issues with shifts that has been paid
        //      shift can still be confirmed existing (error confirms existence)
    }

    xcontext("Week already in the system - introduce the might be out of date context") {
        test("Change in shift") {

        }

        test("Deleted shift") {

        }

        test("Change booking") {

        }

        test("Change supplements (tillæg)") {

        }
    }

    context("Principals") {
        test("Requires the role ${RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN} for single week") {
            weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                year2024Week13,
            )
                .shouldBeRight()

            weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.shiftAdmin,
                year2024Week13,
            )
                .shouldBeRight()

            weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.templateAdmin,
                year2024Week13,
            )
                .shouldBeRight()

            val error = weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.helperAdmin,
                year2024Week13,
            )
                .shouldBeLeft()

            error shouldBe SynchronizationError.InsufficientPermissions(
                RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                PrincipalsTestData.FiktivusMaximus.helperAdmin.roles,
            )
        }

        test("Requires the role ${RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN} for interval") {
            weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.allRoles,
                year2024Week13..year2024Week16,
            )
                .shouldBeRight()

            weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.shiftAdmin,
                year2024Week13..year2024Week16,
            )
                .shouldBeRight()

            weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.templateAdmin,
                year2024Week13..year2024Week16,
            )
                .shouldBeRight()

            val error = weekPlanService.synchronize(
                PrincipalsTestData.FiktivusMaximus.helperAdmin,
                year2024Week13..year2024Week16,
            )
                .shouldBeLeft()

            error shouldBe nonEmptyListOf(
                SynchronizationError.InsufficientPermissions(
                    RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN,
                    PrincipalsTestData.FiktivusMaximus.helperAdmin.roles,
                )
            )
        }
    }
})
