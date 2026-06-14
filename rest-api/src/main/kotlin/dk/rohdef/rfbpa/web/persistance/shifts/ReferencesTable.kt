@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.Reference
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

object ReferencesTable : Table("references") {
    val fromId = uuid("fromId")
        .references(ShiftsTable.id)
    val toId = uuid("toId")
        .references(ShiftsTable.id)
    val linkType = enumeration("linkType", Reference.LinkType::class)

    override val primaryKey = PrimaryKey(fromId, toId, linkType)
}