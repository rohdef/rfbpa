package dk.rohdef.rfbpa

import arrow.core.Either
import dk.rohdef.helperplanning.WeekPlanRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.github.oshai.kotlinlogging.KotlinLogging

class LoggingWeekPlanRepository(
    private val weekPlanRepository: WeekPlanRepository,
) : WeekPlanRepository {
    private val log = KotlinLogging.logger {}

    override suspend fun bookShift(shiftId: ShiftId, helper: Helper.ID): Either<Unit, ShiftId> {
        log.debug { "Booking shift $shiftId to helper ${helper}" }
        return weekPlanRepository.bookShift(shiftId, helper)
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        log.debug { "Reading shifts for $yearWeek" }
        return weekPlanRepository.shifts(yearWeek)
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
        type: ShiftType
    ): Either<Unit, ShiftId> {
        log.debug { "Creating $type shift: ${start.week} ${start.dayOfWeek} ${start.time} -- ${end.time}" }
        val shiftId = weekPlanRepository.createShift(start, end, type)

        when (shiftId) {
            is Either.Right -> log.debug { "Successfully created shift ${shiftId.value}" }
            is Either.Left -> log.error { "Error creating shift ${shiftId.value}" }
        }

        return shiftId
    }
}
