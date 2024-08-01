package dk.rohdef.rfbpa.web.persistance.shifts

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
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
        val booking = if (row[HelpersTable.id] != null) {
            HelperBooking.PermanentHelper(
                Helper(
                    HelperId(row[HelpersTable.id].toKotlinUUID()),
                    row[HelpersTable.shortName],
                )
            )
        } else {
            HelperBooking.NoBooking
        }

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

    override suspend fun byYearWeek(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> = dbQuery {
        val shifts = ShiftsTable
            .leftJoin(ShiftBookingsTable)
            .leftJoin(HelpersTable)
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
        shift: Shift,
    ): Either<ShiftsError, Shift> = dbQuery {
        ShiftsTable.upsert(ShiftsTable.id) {
            it[id] = shift.shiftId.id.toJavaUUID()
            it[startYear] = shift.start.year
            it[startWeek] = shift.start.week
            it[start] = shift.start.localDateTime.toJavaLocalDateTime()
            it[end] = shift.end.localDateTime.toJavaLocalDateTime()
        }

        val helperBooking = shift.helperBooking
        when (helperBooking) {
            HelperBooking.NoBooking -> ShiftBookingsTable.deleteWhere { shiftId eq shift.shiftId.id.toJavaUUID() }
            is HelperBooking.PermanentHelper -> ShiftBookingsTable.upsert(ShiftBookingsTable.shiftId) {
                it[shiftId] = shift.shiftId.id.toJavaUUID()
                it[helperId] = helperBooking.helper.id.id.toJavaUUID()
            }

            is HelperBooking.UnknownHelper -> {
                println("Helper not known: ${helperBooking.externalReference}")
            }
            HelperBooking.VacancyHelper -> TODO()
        }
        shift.right()
    }
}
