package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.Registration
import org.jetbrains.exposed.sql.Table

object RegistrationsTable : Table() {
    val shiftId = uuid("shift_id")
        .references(ShiftsTable.id)
        .uniqueIndex()

    val registration = enumeration("registration", Registration::class)

    override val primaryKey = PrimaryKey(shiftId, registration)
}