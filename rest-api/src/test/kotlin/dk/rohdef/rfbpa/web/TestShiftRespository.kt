package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.raise.either
import dk.rohdef.helperplanning.MemoryShiftRepository
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

typealias CreateShiftErrorRunner = (shift: Shift) -> Either<ShiftsError, Unit>
typealias RepositoryShiftsErrorRunner = (yearWeek: YearWeek) -> Either<ShiftsError, Unit>

class TestShiftRespository(
    val memoryShiftRepository: MemoryShiftRepository = MemoryShiftRepository(),
) : ShiftRepository by memoryShiftRepository {
    private val _createShiftErrorRunners = mutableListOf<CreateShiftErrorRunner>()
    fun addCreateShiftErrorRunner(errorRunner: CreateShiftErrorRunner) {
        _createShiftErrorRunners.add(errorRunner)
    }

    private val _shiftsErrorRunners = mutableListOf<RepositoryShiftsErrorRunner>()
    fun addShiftsErrorRunner(errorRunner: RepositoryShiftsErrorRunner) {
        _shiftsErrorRunners.add(errorRunner)
    }

    fun reset() {
        _createShiftErrorRunners.clear()
        _shiftsErrorRunners.clear()
        memoryShiftRepository.reset()
    }

    internal val shifts: Map<ShiftId, Shift>
        get() = memoryShiftRepository.shifts

    suspend fun addShift(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
    ) {
        memoryShiftRepository.createOrUpdate(subject, shift)
    }

    override suspend fun createOrUpdate(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
    ): Either<ShiftsError, Shift> = either {
        _createShiftErrorRunners.map { it(shift).bind() }
        memoryShiftRepository.createOrUpdate(subject, shift).bind()
    }

    // Work around wrong decendent bug in delegates
    override suspend fun byYearWeekInterval(
        subject: RfbpaPrincipal.Subject,
        yearWeeks: YearWeekInterval,
    ): Either<NonEmptyList<ShiftsError>, List<WeekPlan>> =
        either {
            yearWeeks.mapOrAccumulate { byYearWeek(subject, it).bind() }.bind()
        }

    override suspend fun byYearWeek(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek,
    ): Either<ShiftsError, WeekPlan> = either {
        _shiftsErrorRunners.map { it(yearWeek).bind() }
        memoryShiftRepository.byYearWeek(subject, yearWeek).bind()
    }
}
