@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.persistance.axp

import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

object ShiftReferenceTable : Table() {
    val bookingNumber = varchar("bookingNumber", 255)
    val shiftId = uuid("shift_id").uniqueIndex()

    override val primaryKey = PrimaryKey(bookingNumber)
}
