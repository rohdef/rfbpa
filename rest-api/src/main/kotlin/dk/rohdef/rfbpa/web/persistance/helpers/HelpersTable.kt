package dk.rohdef.rfbpa.web.persistance.helpers

import org.jetbrains.exposed.sql.Table

object HelpersTable : Table() {
    val id = uuid("id")
    val shortName = varchar("short_name", 255).uniqueIndex("HELPERS_SHORT_NAME_UNIQUE_INDEX")

    override val primaryKey = PrimaryKey(id)
}
