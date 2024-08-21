package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import arrow.core.toOption
import dk.rohdef.helperplanning.helpers.HelperId
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

    internal val _shifts = mutableMapOf<RfbpaPrincipal.Subject, MutableMap<ShiftId, Shift>>().withDefault { mutableMapOf() }

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.map { it.value }
            .fold(emptyMap()) { accumulator, value -> accumulator + value }

    override suspend fun bookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<SalarySystemRepository.BookingError, ShiftId> {
        _shifts[subject] = _shifts.getValue(subject)
        val shift = _shifts.getValue(subject)[shiftId].toOption()
            .toEither { SalarySystemRepository.BookingError.ShiftNotFound(shiftId) }
            .flatMap { helper ->
                helpersRepository.byId(helperId)
                    .map { HelperBooking.PermanentHelper(it) }
                    .map { helper.copy(helperBooking = it) }
                    .mapLeft { SalarySystemRepository.BookingError.HelperNotFound(helperId) }

            }
        if (shift is Either.Right) {
            _shifts.getValue(subject)[shiftId] = shift.value
        }

        return shift.map { shiftId }
    }

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, WeekPlan> {
        val shiftsForWeek = _shifts.getValue(subject).values.filter { it.start.yearWeek == yearWeek }
        val weekPlan = WeekPlan(
            yearWeek,
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.MONDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.TUESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.WEDNESDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.THURSDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.FRIDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SATURDAY },
            shiftsForWeek.filter { it.start.dayOfWeek == DayOfWeek.SUNDAY },
        )
        return weekPlan.right()
    }

    override suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<ShiftsError, Shift> {
        _shifts[subject] = _shifts.getValue(subject)
        val shiftId = ShiftId.generateId()
        val shift = Shift(HelperBooking.NoBooking, shiftId, start, end)

        _shifts.getValue(subject)[shiftId] = shift

        return shift.right()
    }
}
