package dk.rohdef.helperplanning

import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId

class TestShiftRespository(
    val memoryShiftRepository: MemoryShiftRepository = MemoryShiftRepository(),
) : ShiftRepository by memoryShiftRepository {
    internal fun reset() = memoryShiftRepository.reset()

    internal val shifts: Map<ShiftId, Shift>
        get() = memoryShiftRepository.shifts
    internal val shiftList: List<Shift>
        get() = shifts.values.toList()
    internal val sortedByStartShifts: List<Shift>
        // TODO: 08/06/2024 rohdef - remove date conversion when implmenting comprable #4
        get() = shiftList.sortedBy { it.start.localDateTime }
}
