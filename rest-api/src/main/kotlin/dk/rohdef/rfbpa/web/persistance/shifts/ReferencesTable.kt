package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.Reference
import org.jetbrains.exposed.sql.Table

object ReferencesTable : Table() {
    val fromId = uuid("fromId")
        .references(ShiftsTable.id)
    val toId = uuid("toId")
        .references(ShiftsTable.id)
    val linkType = enumeration("linkType", Reference.LinkType::class)

    override val primaryKey = PrimaryKey(fromId, toId, linkType)
}