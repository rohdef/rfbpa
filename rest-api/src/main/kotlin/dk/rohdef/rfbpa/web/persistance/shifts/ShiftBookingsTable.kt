package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import org.jetbrains.exposed.sql.Table

object ShiftBookingsTable : Table() {
    val id = uuid("id")
    val helperId = uuid("helper_id").references(HelpersTable.id)
    val shiftId = uuid("helper_id").references(ShiftsTable.id).uniqueIndex()

    override val primaryKey = PrimaryKey(HelpersTable.id)
}
