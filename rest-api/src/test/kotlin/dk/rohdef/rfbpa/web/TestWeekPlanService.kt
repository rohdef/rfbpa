package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval

class TestWeekPlanService : WeekPlanService {
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
        throw NotImplementedError("Method currently not needed for tests")
    }

    override suspend fun synchronize(
        principal: RfbpaPrincipal,
        yearWeek: YearWeek,
    ): Either<SynchronizationError, Unit> {
        throw NotImplementedError("Method currently not needed for tests")
    }

    override suspend fun createShift(
        principal: RfbpaPrincipal,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<WeekPlanServiceError, Shift> {
        throw NotImplementedError("Method currently not needed for tests")
    }

    override suspend fun shifts(
        principal: RfbpaPrincipal,
        yearWeekInterval: YearWeekInterval,
    ): Either<WeekPlanServiceError, List<WeekPlan>> {
        return shiftRepository.byYearWeekInterval(principal.subject, yearWeekInterval)
            .mapLeft { throw IllegalStateException("This should not be possible") }
    }

//    override suspend fun reportIllness(
//        principal: RfbpaPrincipal,
//        shiftId: ShiftId
//    ): Either<WeekPlanServiceError, ShiftId> {
//        TODO("Not yet implemented")
//    }

    override suspend fun changeHelperBooking(
        principal: RfbpaPrincipal,
        shiftId: ShiftId,
        helperBooking: HelperBooking
    ): Either<WeekPlanServiceError, Unit> = either {
        shiftRepository.changeBooking(
            principal.subject,
            shiftId,
            helperBooking
        )
            .mapLeft { WeekPlanServiceError.CannotCommunicateWithShiftsRepository }
            .bind()
    }
}
