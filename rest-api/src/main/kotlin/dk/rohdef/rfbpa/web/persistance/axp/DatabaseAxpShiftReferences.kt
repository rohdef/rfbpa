@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.persistance.axp

import kotlin.uuid.ExperimentalUuidApi

//class DatabaseAxpShiftReferences : AxpShiftReferences {
//    override suspend fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<AxpShiftReferences.ShiftIdNotFound, ShiftId> = dbQuery {
//        ShiftReferenceTable
//            .select(ShiftReferenceTable.shiftId)
//            .where { ShiftReferenceTable.bookingNumber eq axpBookingId.axpId }
//            .map { ShiftId(it[ShiftReferenceTable.shiftId]) }
//            .singleOrNone()
//            .toEither { AxpShiftReferences.ShiftIdNotFound(axpBookingId) }
//    }
//
//    override suspend fun saveAxpBookingToShiftId(bookingNumber: AxpBookingId, shiftId: ShiftId): Either<Unit, Unit> = dbQuery {
//        ShiftReferenceTable.insert {
//            it[ShiftReferenceTable.bookingNumber] = bookingNumber.axpId
//            it[ShiftReferenceTable.shiftId] = shiftId.id
//        }
//        Either.Right(Unit)
//    }
//
//    override suspend fun shiftIdToAxpBooking(shiftId: ShiftId): Either<AxpShiftReferences.BookingIdNotFound, AxpBookingId> = dbQuery {
//        ShiftReferenceTable
//            .select(ShiftReferenceTable.bookingNumber)
//            .where { ShiftReferenceTable.shiftId eq shiftId.id }
//            .map { AxpBookingId(it[ShiftReferenceTable.bookingNumber]) }
//            .singleOrNone()
//            .toEither { AxpShiftReferences.BookingIdNotFound(shiftId) }
//    }
//}
