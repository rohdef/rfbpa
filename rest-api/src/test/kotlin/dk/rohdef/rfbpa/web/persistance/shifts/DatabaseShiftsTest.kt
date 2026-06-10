package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Reference
import dk.rohdef.helperplanning.shifts.Registration
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfbpa.web.PrincipalsTestData
import dk.rohdef.rfbpa.web.persistance.TestDatabaseConnection
import dk.rohdef.rfbpa.web.persistance.helpers.DatabaseHelpers
import dk.rohdef.rfbpa.web.persistance.helpers.TestHelpers
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW29Friday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW29Wednesday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW30Saturday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW30Tuesday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW30Tuesday2
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW31Sunday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW31Wednesday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week29
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week29To31
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week30
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek29
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek30
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek31
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly

class DatabaseShiftsTest : FunSpec({
    val helperRepository = DatabaseHelpers()
    val shiftRepository = DatabaseShifts()

    beforeEach {
        TestDatabaseConnection.init()

        helperRepository.create(TestHelpers.fiktivus)
        helperRepository.create(TestHelpers.realis)
    }

    afterEach {
        TestDatabaseConnection.disconnect()
    }

    val fiktivusPrincipal = PrincipalsTestData.FiktivusMaximus.subject
    test("creating and reading shifts") {
        shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight WeekPlan.emptyPlan(week30)
        shiftRepository.byYearWeekInterval(fiktivusPrincipal, week29To31) shouldBeRight week29To31.map { WeekPlan.emptyPlan(it) }

        shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW29Wednesday1).shouldBeRight()
        shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW29Friday1).shouldBeRight()
        shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Tuesday1).shouldBeRight()
        shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Tuesday2).shouldBeRight()
        shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Saturday1).shouldBeRight()
        shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW31Wednesday1).shouldBeRight()
        shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW31Sunday1).shouldBeRight()

        shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight weekPlanWeek30
        shiftRepository.byYearWeekInterval(fiktivusPrincipal, week29To31) shouldBeRight listOf(
            weekPlanWeek29,
            weekPlanWeek30,
            weekPlanWeek31,
        )
    }

    context("Bookings") {
        test("Shifts with bookings") {
            val fiktivus = TestHelpers.fiktivus
            val shift = shiftW29Wednesday1.copy(helperBooking = HelperBooking.Booked(fiktivus.id))
            shiftRepository.createOrUpdate(fiktivusPrincipal, shift)

            val expectedWeekPlanShift1 = WeekPlan.emptyPlan(week29).copy(wednesday = listOf(shift))
            shiftRepository.byYearWeek(fiktivusPrincipal, week29) shouldBeRight expectedWeekPlanShift1
        }

        test("Shift with no booking becoming booked") {
            val realis = TestHelpers.realis
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Tuesday1)
            val shift = shiftW30Tuesday1.copy(helperBooking = HelperBooking.Booked(realis.id))

            val expectedWeekPlanUnbooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftW30Tuesday1))
            shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight expectedWeekPlanUnbooked
            shiftRepository.createOrUpdate(fiktivusPrincipal, shift)
            val expectedWeekPlanBooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shift))
            shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight expectedWeekPlanBooked
        }

        test("Shift already booked changing helper") {
            val fiktivus = TestHelpers.fiktivus
            val realis = TestHelpers.realis

            val shiftInitial = shiftW30Tuesday1.copy(helperBooking = HelperBooking.Booked(fiktivus.id))
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftInitial)
            val shiftRebooked = shiftW30Tuesday1.copy(helperBooking = HelperBooking.Booked(realis.id))

            val expectedWeekPlanInitial = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftInitial))
            shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight expectedWeekPlanInitial
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftRebooked)
            val expectedWeekPlanUnbooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftRebooked))
            shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight expectedWeekPlanUnbooked
        }

        test("Shift already booked becoming unbooked") {
            val fiktivus = TestHelpers.fiktivus

            val shiftInitial = shiftW30Tuesday1.copy(helperBooking = HelperBooking.Booked(fiktivus.id))
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftInitial)
            val expectedWeekPlanBooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftInitial))
            shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight expectedWeekPlanBooked

            val shiftUnbooked = shiftInitial.copy(helperBooking = HelperBooking.NoBooking)
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftUnbooked)
            val expectedWeekPlanUnbooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftUnbooked))
            shiftRepository.byYearWeek(fiktivusPrincipal, week30) shouldBeRight expectedWeekPlanUnbooked
        }
    }

    context("Registrations") {
        test("of illness") {
            val shiftWithRegistration = shiftW30Tuesday1.copy(
                registrations = listOf(Registration.Illness),
            )

            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftWithRegistration)

            val shift = shiftRepository.byId(fiktivusPrincipal, shiftWithRegistration.shiftId)

            shift shouldBeRight shiftWithRegistration
        }
    }

    context("References") {
        test("by saving") {
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW29Wednesday1)
                .shouldBeRight()
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Tuesday1)
                .shouldBeRight()
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Tuesday2)
                .shouldBeRight()

            val referenceFromShift = shiftW30Tuesday1.copy(
                registrations = listOf(Registration.Illness),
                references = listOf(
                    Reference.From(shiftW29Wednesday1.shiftId, Reference.LinkType.ILLNESS)
                )
            )
            shiftRepository.createOrUpdate(fiktivusPrincipal, referenceFromShift)
                .shouldBeRight()

            val shiftFrom = shiftRepository.byId(fiktivusPrincipal, shiftW30Tuesday1.shiftId)
                .shouldBeRight()
            val shiftTo = shiftRepository.byId(fiktivusPrincipal, shiftW29Wednesday1.shiftId)
                .shouldBeRight()

            shiftFrom.registrations shouldContainExactly listOf(Registration.Illness)
            shiftFrom.references shouldContainExactly listOf(Reference.From(shiftW29Wednesday1.shiftId, Reference.LinkType.ILLNESS))

            shiftTo.registrations.shouldBeEmpty()
            shiftTo.references shouldContainExactly listOf(Reference.To(shiftW30Tuesday1.shiftId, Reference.LinkType.ILLNESS))
        }

        test("by linking") {
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW29Wednesday1)
                .shouldBeRight()
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Tuesday1)
                .shouldBeRight()
            shiftRepository.createOrUpdate(fiktivusPrincipal, shiftW30Tuesday2)
                .shouldBeRight()

            shiftRepository.linkShifts(fiktivusPrincipal, shiftW30Tuesday1.shiftId, shiftW30Tuesday2.shiftId, Reference.LinkType.ILLNESS)
                .shouldBeRight()

            val shiftFrom = shiftRepository.byId(fiktivusPrincipal, shiftW30Tuesday1.shiftId)
                .shouldBeRight()
            val shiftTo = shiftRepository.byId(fiktivusPrincipal, shiftW30Tuesday2.shiftId)
                .shouldBeRight()

            shiftFrom.registrations shouldContainExactly listOf(Registration.Illness)
            shiftFrom.references shouldContainExactly listOf(Reference.From(shiftW30Tuesday2.shiftId, Reference.LinkType.ILLNESS))

            shiftTo.registrations.shouldBeEmpty()
            shiftTo.references shouldContainExactly listOf(Reference.To(shiftW30Tuesday1.shiftId, Reference.LinkType.ILLNESS))
        }
    }

    xtest("Vacancy helpers") {}
})
