package dk.rohdef.helperplanning

import arrow.core.*
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

    val _shifts = mutableMapOf<ShiftId, Shift>()

    val shifts: Map<ShiftId, Shift>
        get() = _shifts.toMap()

    override suspend fun bookShift(
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<SalarySystemRepository.BookingError, ShiftId> {
        val shift = _shifts[shiftId].toOption()
            .toEither { SalarySystemRepository.BookingError.ShiftNotFound(shiftId) }
            .flatMap { helper ->
                helpersRepository.byId(helperId)
                    .map { HelperBooking.PermanentHelper(it) }
                    .map { helper.copy(helperBooking = it) }
                    .mapLeft { SalarySystemRepository.BookingError.HelperNotFound(helperId) }

            }
        if (shift is Either.Right) {
            _shifts[shiftId] = shift.value
        }

        return shift.map { shiftId }
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        val shiftsForWeek = _shifts.values.filter { it.start.yearWeek == yearWeek }
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
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<Unit, Shift> {
        val shiftId = ShiftId.generateId()
        val shift = Shift(HelperBooking.NoBooking, shiftId, start, end)

        _shifts[shiftId] = shift

        return shift.right()
    }
}
