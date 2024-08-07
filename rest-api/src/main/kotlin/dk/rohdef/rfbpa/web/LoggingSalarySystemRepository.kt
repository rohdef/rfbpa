package dk.rohdef.rfbpa.web

import arrow.core.Either
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.github.oshai.kotlinlogging.KotlinLogging

class LoggingSalarySystemRepository(
    private val salarySystemRepository: SalarySystemRepository,
) : SalarySystemRepository {
    private val log = KotlinLogging.logger {}

    override suspend fun bookShift(
        shiftId: ShiftId,
        helper: HelperId,
    ): Either<SalarySystemRepository.BookingError, ShiftId> {
        log.debug { "Booking shift $shiftId to helper ${helper}" }
        return salarySystemRepository.bookShift(shiftId, helper)
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        log.debug { "Reading shifts for $yearWeek" }
        return salarySystemRepository.shifts(yearWeek)
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<Unit, Shift> {
        log.debug { "Creating shift: ${start.week} ${start.dayOfWeek} ${start.time} -- ${end.time}" }
        val shift = salarySystemRepository.createShift(start, end)

        when (shift) {
            is Either.Right -> log.debug { "Successfully created shift ${shift.value}" }
            is Either.Left -> log.error { "Error creating shift ${shift.value}" }
        }

        return shift
    }
}
