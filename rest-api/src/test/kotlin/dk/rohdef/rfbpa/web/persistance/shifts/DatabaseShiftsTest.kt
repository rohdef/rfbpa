package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfbpa.web.persistance.TestDatabaseConnection
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW29Friday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW29Wednesday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW30Saturday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW30Tuesday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW30Tuesday2
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW31Sunday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW31Wednesday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week29To31
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week30
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek29
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek30
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek31
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec

class DatabaseShiftsTest : FunSpec({
    val shiftRepository = DatabaseShifts()

    beforeEach {
        TestDatabaseConnection.init()
    }

    afterEach {
        TestDatabaseConnection.disconnect()
    }

    test("creating and reading shifts") {
        shiftRepository.byYearWeek(week30) shouldBeRight WeekPlan.emptyPlan(week30)
        shiftRepository.byYearWeekInterval(week29To31) shouldBeRight week29To31.map { WeekPlan.emptyPlan(it) }

        shiftRepository.create(shiftW29Wednesday1).shouldBeRight()
        shiftRepository.create(shiftW29Friday1).shouldBeRight()
        shiftRepository.create(shiftW30Tuesday1).shouldBeRight()
        shiftRepository.create(shiftW30Tuesday2).shouldBeRight()
        shiftRepository.create(shiftW30Saturday1).shouldBeRight()
        shiftRepository.create(shiftW31Wednesday1).shouldBeRight()
        shiftRepository.create(shiftW31Sunday1).shouldBeRight()

        shiftRepository.byYearWeek(week30) shouldBeRight weekPlanWeek30
        shiftRepository.byYearWeekInterval(week29To31) shouldBeRight listOf(
            weekPlanWeek29,
            weekPlanWeek30,
            weekPlanWeek31,
        )
    }

    test("Shifts with bookings") {

    }

    xtest("Vacancy helpers") {}
})
