package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.mapOrAccumulate
import arrow.core.raise.either
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval

interface ShiftRepository {
    suspend fun byId(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<ShiftsError, Shift>

    suspend fun byYearWeekInterval(
        subject: RfbpaPrincipal.Subject,
        yearWeeks: YearWeekInterval
    ): Either<NonEmptyList<ShiftsError>, List<WeekPlan>> = either {
        yearWeeks.mapOrAccumulate { byYearWeek(subject, it).bind() }.bind()
    }

    suspend fun byYearWeek(subject: RfbpaPrincipal.Subject, yearWeek: YearWeek): Either<ShiftsError, WeekPlan>

    suspend fun createOrUpdate(subject: RfbpaPrincipal.Subject, shift: Shift): Either<ShiftsError, Shift>

    suspend fun changeBooking(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        booking: HelperBooking.Booked
    ): Either<ShiftsError, Shift>

    suspend fun unbookShift(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<ShiftsError, Unit>

    suspend fun findBooking(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<ShiftsError, HelperId>

    suspend fun linkShifts(
        subject: RfbpaPrincipal.Subject,
        from: ShiftId,
        to: ShiftId,
        linkType: Reference.LinkType
    ): Either<ShiftsError, Unit> = either {
        val fromShift = byId(subject, from).bind()
        val toShift = byId(subject, to).bind()

        // TODO validate and prevent bad links? Might not be worth it if the DB does so

        val linkedFrom = fromShift.copy(references = fromShift.references + Reference.From(to, linkType))
        val linkedTo = toShift.copy(references = fromShift.references + Reference.To(from, linkType))

        createOrUpdate(subject, linkedFrom).bind()
        createOrUpdate(subject, linkedTo).bind()
    }
}
