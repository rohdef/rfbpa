package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfbpa.web.persistance.TestDatabaseConnection
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DayOfWeek
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

internal fun interface ShiftBuilderOnDay {
    fun start(hours: Int, minutes: Int): ShiftBuilderAtStart
}

internal fun interface ShiftBuilderAtStart {
    fun end(hours: Int, minutes: Int): Shift
}

/**
 * This assumes no overlap in shift start/end pairs
 */
internal val shiftIdNamespace = UUID("ffe95790-1bc3-4283-8988-7c16809ac47d")
internal fun generateTestShiftId(start: YearWeekDayAtTime, end: YearWeekDayAtTime): ShiftId {
    val idText = "$start--$end"

    return ShiftId(
        UUID.generateUUID(shiftIdNamespace, idText)
    )
}

internal fun createTestShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Shift {
    return Shift(
        HelperBooking.NoBooking,
        generateTestShiftId(start, end),
        start,
        end,
    )
}

internal fun YearWeek.shift(dayOfWeek: DayOfWeek): ShiftBuilderOnDay {
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

class DatabaseShiftsTest : FunSpec({
    val shiftRepository = DatabaseShifts()

    lateinit var connection: Database
    beforeEach {
        TestDatabaseConnection.init()

    }

    afterEach {
        TestDatabaseConnection.disconnect()
    }


    val year24Week46 = YearWeek(2024, 46)
    val year24Week47 = YearWeek(2024, 47)
    val year24Week48 = YearWeek(2024, 48)

    val week46Shift1 = year24Week46.shift(DayOfWeek.TUESDAY).start(10, 15).end(12, 45)
    val week46Shift2 = year24Week46.shift(DayOfWeek.WEDNESDAY).start(9, 0).end(13, 0)
    val week47Shift1 = year24Week47.shift(DayOfWeek.THURSDAY).start(8, 45).end(14, 15)
    val week47Shift2 = year24Week47.shift(DayOfWeek.FRIDAY).start(7, 30).end(15, 15)
    val week48Shift1 = year24Week48.shift(DayOfWeek.SATURDAY).start(6, 15).end(16, 0)
    val week48Shift2 = year24Week48.shift(DayOfWeek.SUNDAY).start(5, 0).end(17, 45)

    test("creating and reading shifts") {
        val yearWeekInterval = year24Week46..year24Week48

        shiftRepository.shifts(year24Week47) shouldBeRight WeekPlan.emptyPlan(year24Week47)
        shiftRepository.shifts(yearWeekInterval) shouldBeRight yearWeekInterval.map { WeekPlan.emptyPlan(it) }

        shiftRepository.createShift(week46Shift1).shouldBeRight()
        shiftRepository.createShift(week46Shift2).shouldBeRight()
        shiftRepository.createShift(week47Shift1).shouldBeRight()
        shiftRepository.createShift(week47Shift2).shouldBeRight()
        shiftRepository.createShift(week48Shift1).shouldBeRight()
        shiftRepository.createShift(week48Shift2).shouldBeRight()

        shiftRepository.shifts(year24Week47) shouldBeRight WeekPlan.unsafeFromList(
            year24Week47,
            listOf(week47Shift1, week47Shift2),
        )
        shiftRepository.shifts(yearWeekInterval) shouldBeRight listOf(
            WeekPlan.unsafeFromList(
                year24Week46,
                listOf(
                    week46Shift1,
                    week46Shift2,
                ),
            ),
            WeekPlan.unsafeFromList(
                year24Week47,
                listOf(
                    week47Shift1,
                    week47Shift2,
                ),
            ),
            WeekPlan.unsafeFromList(
                year24Week48,
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
