package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.firstOrNone
import arrow.core.mapOrAccumulate
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.helperplanning.shifts.Shift.Companion.copyUnsafe
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval
import kotlinx.datetime.DayOfWeek

class MemoryShiftRepository : ShiftRepository {
    fun reset() {
        _shifts.clear()
    }

    private val _shifts = mutableMapOf<RfbpaPrincipal.Subject, Map<ShiftId, Shift>>().withDefault { emptyMap() }

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.map { it.value }
            .fold(emptyMap()) { accumulator, value -> accumulator + value}

    override suspend fun byId(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<ShiftsError, Shift> = either {
        ensureNotNull(shifts[shiftId]) {
            ShiftsError.ShiftNotFound(shiftId)
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

    override suspend fun byYearWeekInterval(
        subject: RfbpaPrincipal.Subject,
        yearWeeks: YearWeekInterval
    ): Either<NonEmptyList<ShiftsError>, List<WeekPlan>> = either {
        return yearWeeks.mapOrAccumulate {
            byYearWeek(subject, it).bind()
        }
    }

    override suspend fun findBooking(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId
    ): Either<ShiftsError, HelperId> {
        return shifts.values
            .filter { it.shiftId == shiftId }
            .map { it.helperBooking }
            .filterIsInstance<HelperBooking.Booked>()
            .map { it.helper }
            .firstOrNone()
            .toEither { ShiftsError.ShiftNotFound(shiftId) }
    }

    override suspend fun changeBooking(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        booking: HelperBooking.Booked
    ): Either<ShiftsError, Shift> = either {
        val shift = byId(subject, shiftId)
            .map { it.copyUnsafe(helperBooking = booking) }
            .bind()

        createOrUpdate(subject, shift).bind()
    }

    override suspend fun unbookShift(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<ShiftsError, Unit> = either {
        val shift = byId(subject, shiftId)
            .map { it.copyUnsafe(helperBooking = HelperBooking.NoBooking) }
            .bind()

        createOrUpdate(subject, shift).bind()
    }
}