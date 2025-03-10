@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias SalaryShiftsErrorRunner = (yearWeek: YearWeek) -> Either<ShiftsError, Unit>
typealias CreateSalaryShiftErrorRunner = (start: YearWeekDayAtTime, end: YearWeekDayAtTime) -> Either<ShiftsError, Unit>

class TestSalarySystemRepository(
    val memoryWeekPlanRepository: MemorySalarySystemRepository = MemorySalarySystemRepository(),
    var idGenerator: IdGenerator = IdGenerator.Default
) : SalarySystemRepository by memoryWeekPlanRepository {
    interface IdGenerator {
        fun generate(vararg seeds: Any): ShiftId

        object Random : IdGenerator {
            override fun generate(vararg seeds: Any) = ShiftId(Uuid.random())
        }

        object Default : IdGenerator {
            val shiftIdNamespace = Uuid.parse("ffe95790-1bc3-4283-8988-7c16809ac47d")

            override fun generate(vararg seeds: Any): ShiftId {
                val idText = seeds.joinToString(":")

                return ShiftId(
                    Uuid.generateUuid(shiftIdNamespace, idText)
                )
            }
        }
    }

    private val _createShiftErrorRunners = mutableListOf<CreateSalaryShiftErrorRunner>()
    fun addCreateShiftErrorRunner(errorRunner: CreateSalaryShiftErrorRunner) {
        _createShiftErrorRunners.add(errorRunner)
    }

    private val _shiftsErrorRunners = mutableListOf<SalaryShiftsErrorRunner>()
    fun addShiftsErrorRunner(errorRunner: SalaryShiftsErrorRunner) {
        _shiftsErrorRunners.add(errorRunner)
    }

    internal fun reset() {
        memoryWeekPlanRepository.reset()
        _shiftsErrorRunners.clear()
        _createShiftErrorRunners.clear()
    }

    internal val shifts: Map<ShiftId, Shift>
        get() = memoryWeekPlanRepository.shifts
    internal val shiftList: List<Shift>
        get() = shifts.values.toList()
    internal val sortedByStartShifts: List<Shift>
        // TODO: 08/06/2024 rohdef - remove date conversion when implmenting comprable #4
        get() = shiftList.sortedBy { it.start.localDateTime }

    fun addShift(subject: RfbpaPrincipal.Subject, shift: Shift) {
        memoryWeekPlanRepository._shifts.letValue(subject) {
            it + (shift.shiftId to shift)
        }
    }

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, WeekPlan> = either {
        _shiftsErrorRunners.map { it(yearWeek).bind() }
        memoryWeekPlanRepository.shifts(subject, yearWeek).bind()
    }

    override suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime
    ) = either {
        _createShiftErrorRunners.map { it(start, end).bind() }
        memoryWeekPlanRepository._shifts[subject] = memoryWeekPlanRepository._shifts.getValue(subject)
        val shiftId = idGenerator.generate(start, end)
        Shift(HelperBooking.NoBooking, shiftId, start, end)
            .also { shift -> memoryWeekPlanRepository._shifts.letValue(subject) { it + (shiftId to shift) } }
    }

    fun removeShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
    ) {
        memoryWeekPlanRepository._shifts.letValue(subject) { it - shiftId }
    }

    internal fun shiftListOnDay(yearWeekDay: YearWeekDay) =
        shiftList.filter { it.start.yearWeekDay == yearWeekDay }

    internal fun helpersOnDay(yearWeekDay: YearWeekDay): List<HelperBooking> {
        return shiftsOnDay(yearWeekDay).values
            .map { it.helperBooking }
    }

    internal fun shiftsOnDay(yearWeekDay: YearWeekDay): Map<ShiftId, Shift> {
        return shifts.filter { it.value.start.yearWeekDay == yearWeekDay }
    }

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
}
