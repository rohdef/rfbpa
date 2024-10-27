package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import kotlinx.datetime.DayOfWeek

class MemoryShiftRepository : ShiftRepository {
    fun reset() {
        _shifts.clear()
    }

    private val _shifts = mutableMapOf<RfbpaPrincipal.Subject, Map<ShiftId, Shift>>().withDefault { emptyMap() }

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.map { it.value }
            .fold(emptyMap()) { accumulator, value -> accumulator + value}

    private fun byId(shift: ShiftId): Either<ShiftsError, Shift> = either {
        ensureNotNull(shifts[shift]) {
            ShiftsError.ShiftNotFound(shift)
        }
    }

    override suspend fun byYearWeek(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, WeekPlan> = either {
        val shiftsForWeek = _shifts.getValue(subject).values.filter { it.start.yearWeek == yearWeek }
        WeekPlan(
            yearWeek,
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.MONDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.TUESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.WEDNESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.THURSDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.FRIDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SATURDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SUNDAY },
        )
    }

    override suspend fun createOrUpdate(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
    ): Either<ShiftsError, Shift> = either {
        _shifts.letValue(subject) { it + (shift.shiftId to shift) }
        shift
    }

    override suspend fun changeBooking(
        subject: RfbpaPrincipal.Subject,
        shift: ShiftId,
        booking: HelperBooking
    ): Either<ShiftsError, Shift> = either {
        val shift = byId(shift)
            .map { it.copy(helperBooking = booking) }
            .bind()

        createOrUpdate(subject, shift).bind()
    }
}
