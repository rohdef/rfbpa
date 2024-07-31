package dk.rohdef.axpclient

import arrow.core.Either
import dk.rohdef.helperplanning.shifts.ShiftId

interface AxpShiftReferences {
    suspend fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<ShiftIdNotFound, ShiftId>
    suspend fun saveAxpBookingToShiftId(bookingNumber: AxpBookingId, shiftId: ShiftId)
    suspend fun shiftIdToAxpBooking(shiftId: ShiftId): Either<BookingIdNotFound, AxpBookingId>

    data class BookingIdNotFound(val shiftId: ShiftId)
    data class ShiftIdNotFound(val axpBookingId: AxpBookingId)
}
