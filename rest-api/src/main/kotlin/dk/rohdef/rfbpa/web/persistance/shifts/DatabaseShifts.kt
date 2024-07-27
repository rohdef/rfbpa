package dk.rohdef.rfbpa.web.persistance.shifts

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll

class DatabaseShifts : ShiftRepository {
    private fun rowToShift(row: ResultRow): Shift {
        TODO()
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
        return WeekPlan.unsafeFromList(
            yearWeek,
            emptyList(),
        ).right()
    }

    override suspend fun createShift(
        shift: Shift,
    ): Either<ShiftsError, Shift> {
        return shift.right()
    }
}
