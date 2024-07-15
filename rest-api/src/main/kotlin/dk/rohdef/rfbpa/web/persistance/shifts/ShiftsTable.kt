package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ShiftsTable : Table() {
    val id = uuid("id")
    val start = datetime("start")
    val end = datetime("end")

    override val primaryKey = PrimaryKey(HelpersTable.id)
}
