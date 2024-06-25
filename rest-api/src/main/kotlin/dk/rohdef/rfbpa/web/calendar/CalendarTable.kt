package dk.rohdef.rfbpa.web.calendar

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CalendarTable : Table() {
    val id = uuid("id")
    val bookingId = varchar("bookingId", 255)
    val summary = uuid("summary")
    val start = datetime("start")
    val end = datetime("end")

    override val primaryKey = PrimaryKey(id)
}
