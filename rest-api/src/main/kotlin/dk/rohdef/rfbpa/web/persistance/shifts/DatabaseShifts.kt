package dk.rohdef.rfbpa.web.persistance.shifts

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class DatabaseShifts : ShiftRepository {
    private fun rowToShift(row: ResultRow): Shift {
        val helperId = row[ShiftBookingsTable.helperId]?.toKotlinUUID()
            ?.let { HelperId(it) }
        val booking = helperId?.let { HelperBooking.Booked(it) } ?: HelperBooking.NoBooking

        return Shift(
            booking,
            ShiftId(
                row[ShiftsTable.id].toKotlinUUID(),
            ),
            YearWeekDayAtTime.from(
                row[ShiftsTable.start].toKotlinLocalDateTime(),
            ),
            YearWeekDayAtTime.from(
                row[ShiftsTable.end].toKotlinLocalDateTime(),
            ),
        )
    }

    suspend fun byId(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
    ) : Either<ShiftsError, Shift> = dbQuery {
        val shifts = ShiftsTable
            .leftJoin(ShiftBookingsTable)
            .selectAll()
            .where { ShiftsTable.id eq shiftId.id.toJavaUUID() }
            .map { rowToShift(it) }

        when (shifts.size) {
            0 -> ShiftsError.ShiftNotFound(shiftId).left()
            1 -> shifts.first().right()
            else -> TODO("27/10/2024 rohdef - deal properly with many cases")
        }
    }

    override suspend fun byYearWeek(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek,
    ): Either<ShiftsError, WeekPlan> = dbQuery {
        val shifts = ShiftsTable
            .leftJoin(ShiftBookingsTable)
            .selectAll()
            // TODO map selection to date types that exposed can deal with
            .where {
                (ShiftsTable.startYear eq yearWeek.year) and
                        (ShiftsTable.startWeek eq yearWeek.week)
            }
            .map { rowToShift(it) }

        WeekPlan.unsafeFromList(
            yearWeek,
            shifts,
        ).right()
    }

    override suspend fun createOrUpdate(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
    ): Either<ShiftsError, Shift> = dbQuery {
        ShiftsTable.upsert(ShiftsTable.id) {
            it[id] = shift.shiftId.id.toJavaUUID()
            it[startYear] = shift.start.year
            it[startWeek] = shift.start.week
            it[start] = shift.start.localDateTime.toJavaLocalDateTime()
            it[end] = shift.end.localDateTime.toJavaLocalDateTime()
        }

        changeBooking(shift.shiftId, shift.helperBooking)
        shift.right()
    }

    override suspend fun addRegistration(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        registration: Registration
    ): Either<Unit, Shift> {
        TODO("Not yet implemented")
    }

    override suspend fun changeBooking(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        booking: HelperBooking
    ): Either<ShiftsError, Shift> {
        dbQuery {
            changeBooking(shiftId, booking)
        }

        return byId(subject, shiftId)
    }

    private fun changeBooking(
        shift: ShiftId,
        booking: HelperBooking
    ) {
        when (booking) {
            HelperBooking.NoBooking -> ShiftBookingsTable.deleteWhere { shiftId eq shift.id.toJavaUUID() }

            is HelperBooking.Booked -> ShiftBookingsTable.upsert(ShiftBookingsTable.shiftId) {
                it[shiftId] = shift.id.toJavaUUID()
                it[helperId] = booking.helper.id.toJavaUUID()
            }
        }
    }
}
