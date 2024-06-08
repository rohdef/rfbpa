package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.toYearWeekDay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class TestWeekPlanRepository : WeekPlanRepository {
    internal val timezone = TimeZone.of("Europe/Copenhagen")

    internal fun reset() {
        _shifts.clear()
        _bookings.clear()
    }

    private val _shifts = mutableMapOf<ShiftId, Shift>()
    internal val shifts: Map<ShiftId, Shift>
        get() = _shifts.toMap()
    internal val shiftList: List<Shift>
        get() = _shifts.values.toList()
    internal val sortedByStartShifts: List<Shift>
        // TODO: 08/06/2024 rohdef - remove date conversion when implmenting comprable #4
        get() = shiftList.sortedBy { it.start.localDateTime }

    internal fun shiftListOnDay(yearWeekDay: YearWeekDay) =
        shiftList.filter { it.start.yearWeekDay == yearWeekDay }

    internal fun helpersOnDay(yearWeekDay: YearWeekDay): List<HelperBooking> {
        return shiftsOnDay(yearWeekDay).keys
            .map { _bookings.get(it)!! }
    }

    internal fun shiftsOnDay(yearWeekDay: YearWeekDay): Map<ShiftId, Shift> {
        return  _shifts.filter { it.value.start.yearWeekDay == yearWeekDay }
    }

    private val _bookings = mutableMapOf<ShiftId, HelperBooking>().withDefault { HelperBooking.NoBooking }

    internal fun firstShiftStart(): YearWeekDay {
        return this.sortedByStartShifts
            .first()
            .start
            .yearWeekDay
    }

    internal fun lastShiftStart(): YearWeekDay {
        return this.sortedByStartShifts
            .last()
            .start
            .yearWeekDay
    }

    override suspend fun bookShift(shiftId: ShiftId, helper: HelperBooking.PermanentHelper): Either<Unit, ShiftId> {
        _bookings.put(shiftId, helper)

        return shiftId.right()
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        TODO("not implemented")
    }

    override suspend fun createShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime, type: ShiftType): Either<Unit, ShiftId> {
        val uuid = UUID.generateUUID()
        val shiftId = ShiftId(uuid.toString())
        val shift = Shift(start, end, type)

        _shifts.put(shiftId, shift)

        return shiftId.right()
    }

    data class Shift(
        val start: YearWeekDayAtTime,
        val end: YearWeekDayAtTime,
        val type: ShiftType,
    )
}
