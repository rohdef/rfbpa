@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalUuidApi::class)

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
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class DatabaseShifts : ShiftRepository {
    private val log = KotlinLogging.logger { }

    private fun rowToShift(row: ResultRow): Shift {
        val helperId = row[ShiftBookingsTable.helperId]?.toKotlinUuid()
            ?.let { HelperId(it) }
        val booking = helperId?.let { HelperBooking.Booked(it) } ?: HelperBooking.NoBooking

        return Shift(
            booking,
            ShiftId(
                row[ShiftsTable.id].toKotlinUuid(),
            ),
            YearWeekDayAtTime.from(
                row[ShiftsTable.start].toKotlinLocalDateTime(),
            ),
            YearWeekDayAtTime.from(
                row[ShiftsTable.end].toKotlinLocalDateTime(),
            ),
        )
    }

    override suspend fun byId(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
    ) : Either<ShiftsError, Shift> = dbQuery {
        val shifts = ShiftsTable
            .leftJoin(ShiftBookingsTable)
            .selectAll()
            .where { ShiftsTable.id eq shiftId.id.toJavaUuid() }
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
            it[id] = shift.shiftId.id.toJavaUuid()
            it[startYear] = shift.start.year
            it[startWeek] = shift.start.week
            it[start] = shift.start.localDateTime.toJavaLocalDateTime()
            it[end] = shift.end.localDateTime.toJavaLocalDateTime()
        }

        changeBooking(shift.shiftId, shift.helperBooking)
        shift.right()
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
            HelperBooking.NoBooking -> ShiftBookingsTable.deleteWhere { shiftId eq shift.id.toJavaUuid() }

            is HelperBooking.Booked -> ShiftBookingsTable.upsert(ShiftBookingsTable.shiftId) {
                it[shiftId] = shift.id.toJavaUuid()
                it[helperId] = booking.helper.id.toJavaUuid()
            }
        }
    }
}
