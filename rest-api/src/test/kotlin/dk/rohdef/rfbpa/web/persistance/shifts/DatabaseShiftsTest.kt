package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfbpa.web.persistance.TestDatabaseConnection
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shift
import dk.rohdef.rfweeks.YearWeek
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import kotlinx.datetime.DayOfWeek

class DatabaseShiftsTest : FunSpec({
    val shiftRepository = DatabaseShifts()

    beforeEach {
        TestDatabaseConnection.init()
    }

    afterEach {
        TestDatabaseConnection.disconnect()
    }

    val week46 = YearWeek(2024, 46)
    val week47 = YearWeek(2024, 47)
    val week48 = YearWeek(2024, 48)

    val week46Shift1 = week46.shift(DayOfWeek.TUESDAY).start(10, 15).end(12, 45)
    val week46Shift2 = week46.shift(DayOfWeek.WEDNESDAY).start(9, 0).end(13, 0)
    val week47Shift1 = week47.shift(DayOfWeek.THURSDAY).start(8, 45).end(14, 15)
    val week47Shift2 = week47.shift(DayOfWeek.FRIDAY).start(7, 30).end(15, 15)
    val week48Shift1 = week48.shift(DayOfWeek.SATURDAY).start(6, 15).end(16, 0)
    val week48Shift2 = week48.shift(DayOfWeek.SUNDAY).start(5, 0).end(17, 45)

    test("creating and reading shifts") {
        val week46To48 = week46..week48

        shiftRepository.shifts(week47) shouldBeRight WeekPlan.emptyPlan(week47)
        shiftRepository.shifts(week46To48) shouldBeRight week46To48.map { WeekPlan.emptyPlan(it) }

        shiftRepository.createShift(week46Shift1).shouldBeRight()
        shiftRepository.createShift(week46Shift2).shouldBeRight()
        shiftRepository.createShift(week47Shift1).shouldBeRight()
        shiftRepository.createShift(week47Shift2).shouldBeRight()
        shiftRepository.createShift(week48Shift1).shouldBeRight()
        shiftRepository.createShift(week48Shift2).shouldBeRight()

        shiftRepository.shifts(week47) shouldBeRight WeekPlan.unsafeFromList(
            week47,
            listOf(week47Shift1, week47Shift2),
        )
        shiftRepository.shifts(week46To48) shouldBeRight listOf(
            WeekPlan.unsafeFromList(
                week46,
                listOf(
                    week46Shift1,
                    week46Shift2,
                ),
            ),
            WeekPlan.unsafeFromList(
                week47,
                listOf(
                    week47Shift1,
                    week47Shift2,
                ),
            ),
            WeekPlan.unsafeFromList(
                week48,
                listOf(
                    week48Shift1,
                    week48Shift2,
                ),
            ),
        )
    }

    xtest("Shifts with bookings") {}

    xtest("Vacancy helpers") {}
})
