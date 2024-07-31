package dk.rohdef.rfbpa.web.persistance.shifts

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.*

class DatabaseShifts : ShiftRepository {
    private fun rowToShift(row: ResultRow): Shift {
        return Shift(
            HelperBooking.NoBooking,
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
//            .innerJoin(
//                ShiftBookingsTable,
//                { id },
//                { shiftId },
//            )
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

    override suspend fun create(
        shift: Shift,
    ): Either<ShiftsError, Shift> = dbQuery {
        ShiftsTable.insert {
            it[id] = shift.shiftId.id.toJavaUUID()
            it[startYear] = shift.start.year
            it[startWeek] = shift.start.week
            it[start] = shift.start.localDateTime.toJavaLocalDateTime()
            it[end] = shift.end.localDateTime.toJavaLocalDateTime()
        }
        shift.right()
    }
}
