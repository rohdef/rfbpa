package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.withError
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.DayOfWeek

class MemorySalarySystemRepository(
    val helpersRepository: HelpersRepository = MemoryHelpersRepository(),
) : SalarySystemRepository {
    fun reset() {
        _shifts.clear()
    }

    // TODO: 21/09/2024 rohdef - find isolation level - internal doesn't work
    val _shifts =
        mutableMapOf<RfbpaPrincipal.Subject, Map<ShiftId, Shift>>().withDefault { emptyMap() }

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.map { it.value }
            .fold(emptyMap()) { accumulator, value -> accumulator + value }

    override suspend fun bookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<SalarySystemRepository.BookingError, ShiftId> = either {
        val helperBooking = withError({ SalarySystemRepository.BookingError.HelperNotFound(helperId) }) {
            helpersRepository.byId(helperId)
                .map { HelperBooking.Booked(it) }
                .bind()
        }

        val shift = ensureNotNull(_shifts.getValue(subject)[shiftId]) {
            SalarySystemRepository.BookingError.ShiftNotFound(shiftId)
        }.copy(helperBooking = helperBooking)

        _shifts.letValue(subject) { it + (shiftId to shift) }

        shiftId
    }

    override suspend fun shifts(
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

    override suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<ShiftsError, Shift> = either {
        _shifts[subject] = _shifts.getValue(subject)
        Shift(HelperBooking.NoBooking, ShiftId.generateId(), start, end)
            .also { shift ->
                _shifts.letValue(subject) { it + (shift.shiftId to shift)}
            }
    }
}
