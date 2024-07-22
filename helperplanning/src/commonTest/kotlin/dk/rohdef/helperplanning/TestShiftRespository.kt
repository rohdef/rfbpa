package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId

typealias CreateShiftPrerunner = (shift: Shift) -> Unit

class TestShiftRespository(
    val memoryShiftRepository: MemoryShiftRepository = MemoryShiftRepository(),
) : ShiftRepository by memoryShiftRepository {
    private val _createShiftPrerunners = mutableListOf<CreateShiftPrerunner>()
    fun addCreateShiftPrerunner(prerunner: CreateShiftPrerunner) {
        _createShiftPrerunners.add(prerunner)
    }

    internal fun reset() = memoryShiftRepository.reset()

    internal val shifts: Map<ShiftId, Shift>
        get() = memoryShiftRepository.shifts
    internal val shiftList: List<Shift>
        get() = shifts.values.toList()

    override suspend fun createShift(shift: Shift): Either<Unit, Shift> {
        _createShiftPrerunners.forEach { it(shift) }
        return memoryShiftRepository.createShift(shift)
    }
}
