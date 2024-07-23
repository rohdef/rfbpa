package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId

typealias CreateShiftPreRunner = (shift: Shift) -> Unit
typealias CreateShiftErrorRunner = (shift: Shift) -> Either<Unit, Unit>

class TestShiftRespository(
    val memoryShiftRepository: MemoryShiftRepository = MemoryShiftRepository(),
) : ShiftRepository by memoryShiftRepository {
    private val _createShiftPreRunners = mutableListOf<CreateShiftPreRunner>()
    fun addCreateShiftPreRunner(preRunner: CreateShiftPreRunner) {
        _createShiftPreRunners.add(preRunner)
    }
    private val _createShiftErrorRunners = mutableListOf<CreateShiftErrorRunner>()
    fun addCreateShiftErrorRunner(errorRunner: CreateShiftErrorRunner) {
        _createShiftErrorRunners.add(errorRunner)
    }

    fun reset() = memoryShiftRepository.reset()

    internal val shifts: Map<ShiftId, Shift>
        get() = memoryShiftRepository.shifts
    internal val shiftList: List<Shift>
        get() = shifts.values.toList()

    override suspend fun createShift(shift: Shift): Either<Unit, Shift> = either {
        _createShiftPreRunners.forEach { it(shift) }
        _createShiftErrorRunners.map { it(shift).bind() }
        memoryShiftRepository.createShift(shift).bind()
    }
}
