package dk.rohdef.rfbpa.web.persistance.shifts

import arrow.core.Either
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfbpa.web.persistance.shifts.ShiftsTable.innerJoin
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import org.jetbrains.exposed.sql.*

class DatabaseShifts: ShiftRepository {
    private fun rowToShift(row: ResultRow): Shift {
        TODO()
    }

    override suspend fun bookShift(shiftId: ShiftId, helperId: Helper.ID): Either<Unit, ShiftId> {
        TODO("not implemented")
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        val shifts = ShiftsTable
            .innerJoin(
                ShiftBookingsTable.alias("sbt"),
                { id },
                { ShiftBookingsTable.shiftId },
            )
            .selectAll()
        // TODO map selection to date types that exposed can deal with
//            .where {  }
//            .
        TODO("not implemented")
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
        type: ShiftType
    ): Either<Unit, ShiftId> {
        TODO("not implemented")
    }
}
