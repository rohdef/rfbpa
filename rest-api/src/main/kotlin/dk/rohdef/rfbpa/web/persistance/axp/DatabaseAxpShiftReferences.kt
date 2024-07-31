package dk.rohdef.rfbpa.web.persistance.axp

import arrow.core.Either
import arrow.core.singleOrNone
import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.AxpShiftReferences
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.insert

class DatabaseAxpShiftReferences : AxpShiftReferences {
    override suspend fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<Unit, ShiftId> = dbQuery {
        ShiftReferenceTable
            .select(ShiftReferenceTable.shiftId)
            .where { ShiftReferenceTable.bookingNumber eq axpBookingId.axpId }
            .map { ShiftId(it[ShiftReferenceTable.shiftId].toKotlinUUID()) }
            .singleOrNone()
            .toEither { }
    }

    override suspend fun saveAxpBookingToShiftId(bookingNumber: AxpBookingId, shiftId: ShiftId) = dbQuery {
        ShiftReferenceTable.insert {
            it[ShiftReferenceTable.bookingNumber] = bookingNumber.axpId
            it[ShiftReferenceTable.shiftId] = shiftId.id.toJavaUUID()
        }
        Unit
    }

    override suspend fun shiftIdToAxpBooking(shiftId: ShiftId): Either<Unit, AxpBookingId> = dbQuery {
        ShiftReferenceTable
            .select(ShiftReferenceTable.bookingNumber)
            .where { ShiftReferenceTable.shiftId eq shiftId.id.toJavaUUID() }
            .map { AxpBookingId(it[ShiftReferenceTable.bookingNumber]) }
            .singleOrNone()
            .toEither { }
    }

    data class BookingIdNotFound(val axpBookingId: AxpBookingId)
    data class ShiftIdNotFound(val shiftId: ShiftId)
}
