package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.WeekPlan
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

    test("creating and reading shifts") {
        shiftRepository.byYearWeek(week30) shouldBeRight WeekPlan.emptyPlan(week30)
        shiftRepository.byYearWeekInterval(week29To31) shouldBeRight week29To31.map { WeekPlan.emptyPlan(it) }

        shiftRepository.createOrUpdate(shiftW29Wednesday1).shouldBeRight()
        shiftRepository.createOrUpdate(shiftW29Friday1).shouldBeRight()
        shiftRepository.createOrUpdate(shiftW30Tuesday1).shouldBeRight()
        shiftRepository.createOrUpdate(shiftW30Tuesday2).shouldBeRight()
        shiftRepository.createOrUpdate(shiftW30Saturday1).shouldBeRight()
        shiftRepository.createOrUpdate(shiftW31Wednesday1).shouldBeRight()
        shiftRepository.createOrUpdate(shiftW31Sunday1).shouldBeRight()

        shiftRepository.byYearWeek(week30) shouldBeRight weekPlanWeek30
        shiftRepository.byYearWeekInterval(week29To31) shouldBeRight listOf(
            weekPlanWeek29,
            weekPlanWeek30,
            weekPlanWeek31,
        )
    }

    context("Bookings") {
        test("Shifts with bookings") {
            val fiktivus = TestHelpers.fiktivus
            val shift = shiftW29Wednesday1.copy(helperBooking = HelperBooking.PermanentHelper(fiktivus))
            shiftRepository.createOrUpdate(shift)

            val expectedWeekPlanShift1 = WeekPlan.emptyPlan(week29).copy(wednesday = listOf(shift))
            shiftRepository.byYearWeek(week29) shouldBeRight expectedWeekPlanShift1
        }

        test("Shift with no booking becoming booked") {
            val realis = TestHelpers.realis
            shiftRepository.createOrUpdate(shiftW30Tuesday1)
            val shift = shiftW30Tuesday1.copy(helperBooking = HelperBooking.PermanentHelper(realis))

            val expectedWeekPlanUnbooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftW30Tuesday1))
            shiftRepository.byYearWeek(week30) shouldBeRight expectedWeekPlanUnbooked
            shiftRepository.createOrUpdate(shift)
            val expectedWeekPlanBooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shift))
            shiftRepository.byYearWeek(week30) shouldBeRight expectedWeekPlanBooked
        }

        test("Shift already booked changing helper") {
            val fiktivus = TestHelpers.fiktivus
            val realis = TestHelpers.realis

            val shiftInitial = shiftW30Tuesday1.copy(helperBooking = HelperBooking.PermanentHelper(fiktivus))
            shiftRepository.createOrUpdate(shiftInitial)
            val shiftRebooked = shiftW30Tuesday1.copy(helperBooking = HelperBooking.PermanentHelper(realis))

            val expectedWeekPlanInitial = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftInitial))
            shiftRepository.byYearWeek(week30) shouldBeRight expectedWeekPlanInitial
            shiftRepository.createOrUpdate(shiftRebooked)
            val expectedWeekPlanUnbooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftRebooked))
            shiftRepository.byYearWeek(week30) shouldBeRight expectedWeekPlanUnbooked
        }

        test("Shift already booked becoming unbooked") {
            val fiktivus = TestHelpers.fiktivus

            val shiftInitial = shiftW30Tuesday1.copy(helperBooking = HelperBooking.PermanentHelper(fiktivus))
            shiftRepository.createOrUpdate(shiftInitial)
            val expectedWeekPlanBooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftInitial))
            shiftRepository.byYearWeek(week30) shouldBeRight expectedWeekPlanBooked

            val shiftUnbooked = shiftInitial.copy(helperBooking = HelperBooking.NoBooking)
            shiftRepository.createOrUpdate(shiftUnbooked)
            val expectedWeekPlanUnbooked = WeekPlan.emptyPlan(week30).copy(tuesday = listOf(shiftUnbooked))
            shiftRepository.byYearWeek(week30) shouldBeRight expectedWeekPlanUnbooked
        }
    }

    xtest("Vacancy helpers") {}
})
