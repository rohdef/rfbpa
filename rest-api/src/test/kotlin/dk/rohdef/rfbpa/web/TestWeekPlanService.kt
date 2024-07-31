package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.NonEmptyList
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

class TestWeekPlanService  : WeekPlanService {
    val shiftRepository = TestShiftRespository()

    internal fun reset() {
        shiftRepository.reset()
    }

    internal suspend fun addShift(shift: Shift) {
        shiftRepository.addShift(shift)
    }

    override suspend fun synchronize(yearWeekInterval: YearWeekInterval): Either<NonEmptyList<SynchronizationError>, Unit> {
        TODO("not implemented")
    }

    override suspend fun synchronize(yearWeek: YearWeek): Either<SynchronizationError, Unit> {
        TODO("not implemented")
    }

    override suspend fun createShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime): Either<Unit, Shift> {
        TODO("not implemented")
    }

    override suspend fun shifts(yearWeekInterval: YearWeekInterval): Either<WeekPlanServiceError, List<WeekPlan>> {
        return shiftRepository.byYearWeekInterval(yearWeekInterval)
            .mapLeft { throw IllegalStateException("This should not be possible") }
    }
}
