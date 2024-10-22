package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.NonEmptyList
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

class TestWeekPlanService  : WeekPlanService {
    val shiftRepository = TestShiftRespository()

    internal fun reset() {
        shiftRepository.reset()
    }

    internal suspend fun addShift(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
    ) {
        shiftRepository.addShift(subject, shift)
    }

    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval,
    ): Either<NonEmptyList<SynchronizationError>, Unit> {
        TODO("not implemented")
    }

    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeek: YearWeek,
    ): Either<SynchronizationError, Unit> {
        TODO("not implemented")
    }

    override suspend fun createShift(
        principal: RfbpaPrincipal,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<WeekPlanServiceError, Shift> {
        TODO("not implemented")
    }

    override suspend fun shifts(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval,
    ): Either<WeekPlanServiceError, List<WeekPlan>> {
        return shiftRepository.byYearWeekInterval(principal.subject, yearWeekInterval)
            .mapLeft { throw IllegalStateException("This should not be possible") }
    }
}
