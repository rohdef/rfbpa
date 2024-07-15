package dk.rohdef.rfbpa.web.persistance.helpers

import org.jetbrains.exposed.sql.Table

object HelpersTable : Table() {
    val id = uuid("id")
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}
