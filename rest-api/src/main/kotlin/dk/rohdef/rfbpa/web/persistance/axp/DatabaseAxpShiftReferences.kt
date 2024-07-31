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
    override suspend fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<AxpShiftReferences.ShiftIdNotFound, ShiftId> = dbQuery {
        ShiftReferenceTable
            .select(ShiftReferenceTable.shiftId)
            .where { ShiftReferenceTable.bookingNumber eq axpBookingId.axpId }
            .map { ShiftId(it[ShiftReferenceTable.shiftId].toKotlinUUID()) }
            .singleOrNone()
            .toEither { AxpShiftReferences.ShiftIdNotFound(axpBookingId) }
    }

    override suspend fun saveAxpBookingToShiftId(bookingNumber: AxpBookingId, shiftId: ShiftId) = dbQuery {
        ShiftReferenceTable.insert {
            it[ShiftReferenceTable.bookingNumber] = bookingNumber.axpId
            it[ShiftReferenceTable.shiftId] = shiftId.id.toJavaUUID()
        }
        Unit
    }

    override suspend fun shiftIdToAxpBooking(shiftId: ShiftId): Either<AxpShiftReferences.BookingIdNotFound, AxpBookingId> = dbQuery {
        ShiftReferenceTable
            .select(ShiftReferenceTable.bookingNumber)
            .where { ShiftReferenceTable.shiftId eq shiftId.id.toJavaUUID() }
            .map { AxpBookingId(it[ShiftReferenceTable.bookingNumber]) }
            .singleOrNone()
            .toEither { AxpShiftReferences.BookingIdNotFound(shiftId) }
    }
}
