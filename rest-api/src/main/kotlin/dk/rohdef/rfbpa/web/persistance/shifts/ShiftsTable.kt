package dk.rohdef.rfbpa.web.persistance.shifts

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object ShiftsTable : Table() {
    val id = uuid("id")
    val startYear = integer("start_year")
    val startWeek = integer("start_week")
    val start = datetime("start")
    val end = datetime("end")

    override val primaryKey = PrimaryKey(id)
}
