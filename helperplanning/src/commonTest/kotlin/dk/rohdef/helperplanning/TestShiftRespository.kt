package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime

class TestShiftRespository(
    val memoryShiftRepository: MemoryShiftRepository = MemoryShiftRepository(),
) : ShiftRepository {
    internal fun reset() = memoryShiftRepository.reset()

    internal val shifts: Map<ShiftId, Shift>
        get() = memoryShiftRepository.shifts
    internal val shiftList: List<Shift>
        get() = shifts.values.toList()
    internal val sortedByStartShifts: List<Shift>
        // TODO: 08/06/2024 rohdef - remove date conversion when implmenting comprable #4
        get() = shiftList.sortedBy { it.start.localDateTime }

    override suspend fun bookShift(shiftId: ShiftId, helperId: Helper.ID): Either<Unit, ShiftId> {
        TODO("not implemented")
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        TODO("not implemented")
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<Unit, ShiftId> {
        TODO("not implemented")
    }
}
