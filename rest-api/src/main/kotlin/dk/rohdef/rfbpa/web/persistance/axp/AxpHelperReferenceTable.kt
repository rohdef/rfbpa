package dk.rohdef.rfbpa.web.persistance.axp

import org.jetbrains.exposed.sql.Table

object AxpHelperReferenceTable : Table() {
    val helperId = uuid("helper_id")
        .uniqueIndex()
        .index()

    val helperTid = varchar("helper_tid", 128)
        .nullable()
        .uniqueIndex()
        .index()

    val helperNumber = varchar("helper_number", 128)
        .uniqueIndex()
        .index()

    override val primaryKey = PrimaryKey(helperNumber)
}