package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import org.jetbrains.exposed.sql.Table

object ShiftBookingsTable : Table() {
    val shiftId = uuid("shift_id")
        .references(ShiftsTable.id)
        .uniqueIndex()
    val helperId = uuid("helper_id")
        .references(HelpersTable.id)

    override val primaryKey = PrimaryKey(shiftId)
}
