package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface ShiftRepository {
    suspend fun byYearWeekInterval(subject: RfbpaPrincipal.Subject, yearWeeks: YearWeekInterval): Either<NonEmptyList<ShiftsError>, List<WeekPlan>> = either {
        yearWeeks.mapOrAccumulate { byYearWeek(subject, it).bind() }.bind()
    }

    suspend fun byYearWeek(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun createOrUpdate(subject: RfbpaPrincipal.Subject, shift: Shift): Either<ShiftsError, Shift>

    suspend fun changeBooking(subject: RfbpaPrincipal.Subject, shift: ShiftId, booking: HelperBooking): Either<ShiftsError, Shift>
}
