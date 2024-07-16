package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import kotlinx.datetime.DateTimePeriod
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class MemorySalarySystemRepository : SalarySystemRepository {
    fun reset() {
        _shifts.clear()
        _bookings.clear()
    }

    private val _shifts = mutableMapOf<ShiftId, MemoryShift>()
    private val _bookings = mutableMapOf<ShiftId, HelperBooking>().withDefault { HelperBooking.NoBooking }

    val shifts: Map<ShiftId, MemoryShift>
        get() = _shifts.toMap()
    val bookings: Map<ShiftId, HelperBooking>
        get() = _bookings.toMap().withDefault { HelperBooking.NoBooking }

    override suspend fun cacheMisses(
        yearWeeks: YearWeekInterval,
        updateStrategy: SalarySystemRepository.UpdateStrategy,
        threshold: DateTimePeriod
    ): Either<Unit, Set<YearWeek>> {
        TODO("not implemented")
    }

    override suspend fun bookShift(shiftId: ShiftId, helper: Helper.ID): Either<Unit, ShiftId> {
        if (!_shifts.containsKey(shiftId)) {
            TODO("Missing shift is currently not handled")
        }
        _bookings.put(shiftId, HelperBooking.PermanentHelper(helper))

        return shiftId.right()
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        TODO("not implemented")
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<Unit, ShiftId> {
        val uuid = UUID.generateUUID()
        val shiftId = ShiftId(uuid.toString())
        val shift = MemoryShift(start, end)

        _shifts.put(shiftId, shift)

        return shiftId.right()
    }

    data class MemoryShift(
        val start: YearWeekDayAtTime,
        val end: YearWeekDayAtTime,
    )
}
