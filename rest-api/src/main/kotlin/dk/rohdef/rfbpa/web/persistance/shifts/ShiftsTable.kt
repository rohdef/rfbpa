@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.persistance.shifts

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import kotlin.uuid.ExperimentalUuidApi

object ShiftsTable : Table() {
    val id = uuid("id")
    val startYear = integer("start_year")
    val startWeek = integer("start_week")
    val start = datetime("start")
    val end = datetime("end")

    override val primaryKey = PrimaryKey(id)
}
