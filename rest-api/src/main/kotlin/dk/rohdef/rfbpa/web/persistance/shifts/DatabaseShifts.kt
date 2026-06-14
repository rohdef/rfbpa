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
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.ExperimentalUuidApi

class DatabaseShifts : ShiftRepository {
    private fun rowToShift(row: ResultRow): Shift {
        val shiftIdValue = row[ShiftsTable.id]
        val helperId = row[ShiftBookingsTable.helperId]
            ?.let { HelperId(it) }
        val booking = helperId?.let { HelperBooking.Booked(it) } ?: HelperBooking.NoBooking

        val registrations = RegistrationsTable
            .selectAll()
            .where { RegistrationsTable.shiftId eq shiftIdValue }
            .map { it[RegistrationsTable.registration] }

        val references = ReferencesTable
            .selectAll()
            .where { ReferencesTable.fromId eq shiftIdValue }
            .map {
                val toId = ShiftId(it[ReferencesTable.toId])
                val linkType = it[ReferencesTable.linkType]

                Reference.From(toId, linkType)
            } + ReferencesTable
            .selectAll()
            .where { ReferencesTable.toId eq shiftIdValue }
            .map {
                val fromId = ShiftId(it[ReferencesTable.fromId])
                val linkType = it[ReferencesTable.linkType]

                Reference.To(fromId, linkType)
            }

        return Shift.createUnsafe(
            booking,
            ShiftId(shiftIdValue),
            YearWeekDayAtTime.from(
                row[ShiftsTable.start].toKotlinLocalDateTime(),
            ),
            YearWeekDayAtTime.from(
                row[ShiftsTable.end].toKotlinLocalDateTime(),
            ),
            registrations,
            references,
        )
    }

    override suspend fun byId(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
    ) : Either<ShiftsError, Shift> = dbQuery {
        val shifts = ShiftsTable
            .leftJoin(ShiftBookingsTable)
            .selectAll()
            .where { ShiftsTable.id eq shiftId.id }
            .limit(2)
            .map { rowToShift(it) }

        when (shifts.size) {
            0 -> ShiftsError.ShiftNotFound(shiftId).left()
            1 -> shifts.first().right()
            else -> throw IllegalStateException("More than one shift found for id: $shiftId. Only 0 or 1 shifts are allowed.")
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
            it[id] = shift.shiftId.id
            it[startYear] = shift.start.year
            it[startWeek] = shift.start.week
            it[start] = shift.start.localDateTime.toJavaLocalDateTime()
            it[end] = shift.end.localDateTime.toJavaLocalDateTime()
        }

        RegistrationsTable.batchUpsert(
            shift.registrations,
        ) {
            this[RegistrationsTable.shiftId] = shift.shiftId.id
            this[RegistrationsTable.registration] = it
        }

        ReferencesTable.batchUpsert(
            shift.references,
        ) {
            this[ReferencesTable.fromId] = shift.shiftId.id
            this[ReferencesTable.toId] = it.id.id
            this[ReferencesTable.linkType] = it.linkType
        }

        val booking = shift.helperBooking
        when (booking) {
            is HelperBooking.Booked -> changeBooking(shift.shiftId, booking)
            HelperBooking.NoBooking -> unbookShift(shift.shiftId)
        }

        shift.right()
    }

    override suspend fun changeBooking(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        booking: HelperBooking.Booked,
    ): Either<ShiftsError, Shift> {
        dbQuery {
            changeBooking(shiftId, booking)
        }

        return byId(subject, shiftId)
    }

    override suspend fun findBooking(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId
    ): Either<ShiftsError, HelperId> {
        val helperIds = dbQuery {
            ShiftBookingsTable
                .selectAll()
                .where { ShiftBookingsTable.shiftId eq shiftId.id }
                .limit(2)
                .map { it[ShiftBookingsTable.helperId] }
                .map { HelperId(it) }
        }

        return when (helperIds.size) {
            0 -> ShiftsError.ShiftNotFound(shiftId).left()
            1 -> helperIds.first().right()
            else -> throw IllegalStateException("More than one helper booking found for id: $shiftId. Only 0 or 1 shifts are allowed.")
        }
    }

    private fun changeBooking(
        id: ShiftId,
        booking: HelperBooking.Booked,
    ) {
        ShiftBookingsTable.upsert(ShiftBookingsTable.shiftId) {
            it[shiftId] = id.id
            it[helperId] = booking.helper.value
        }
    }

    override suspend fun unbookShift(subject: RfbpaPrincipal.Subject, shiftId: ShiftId): Either<ShiftsError, Unit> = dbQuery {
        unbookShift(shiftId)
        Unit.right()
    }

    private fun unbookShift(id: ShiftId) {
        ShiftBookingsTable.deleteWhere { shiftId eq id.id }
    }
}
