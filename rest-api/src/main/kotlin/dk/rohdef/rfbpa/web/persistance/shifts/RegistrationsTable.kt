@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.persistance.shifts

import dk.rohdef.helperplanning.shifts.Registration
import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

object RegistrationsTable : Table("registrations") {
    val shiftId = uuid("shift_id")
        .references(ShiftsTable.id)
        .uniqueIndex()

    val registration = enumeration("registration", Registration::class)

    override val primaryKey = PrimaryKey(shiftId, registration)
}