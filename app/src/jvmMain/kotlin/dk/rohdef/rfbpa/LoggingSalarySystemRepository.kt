package dk.rohdef.rfbpa

import arrow.core.Either
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.DateTimePeriod

class LoggingSalarySystemRepository(
    private val salarySystemRepository: SalarySystemRepository,
) : SalarySystemRepository {
    private val log = KotlinLogging.logger {}

    override suspend fun cacheMisses(
        yearWeeks: YearWeekInterval,
        updateStrategy: SalarySystemRepository.UpdateStrategy,
        threshold: DateTimePeriod
    ): Either<Unit, Set<YearWeek>> {
        log.debug { "Finding the weeks that need synchronization" }
        return salarySystemRepository.cacheMisses(yearWeeks, updateStrategy, threshold)
    }

    override suspend fun bookShift(shiftId: ShiftId, helper: Helper.ID): Either<Unit, ShiftId> {
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
    ): Either<Unit, ShiftId> {
        log.debug { "Creating shift: ${start.week} ${start.dayOfWeek} ${start.time} -- ${end.time}" }
        val shiftId = salarySystemRepository.createShift(start, end)

        when (shiftId) {
            is Either.Right -> log.debug { "Successfully created shift ${shiftId.value}" }
            is Either.Left -> log.error { "Error creating shift ${shiftId.value}" }
        }

        return shiftId
    }
}
