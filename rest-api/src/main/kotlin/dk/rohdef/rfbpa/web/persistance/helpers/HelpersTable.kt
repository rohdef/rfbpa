package dk.rohdef.rfbpa.web.persistance.helpers

import org.jetbrains.exposed.sql.Table

object HelpersTable : Table() {
    val id = uuid("id")
    val name = varchar("name", 255)
    val shortName = varchar("short_name", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}
