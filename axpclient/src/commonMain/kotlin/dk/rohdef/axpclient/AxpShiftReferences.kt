package dk.rohdef.axpclient

import arrow.core.Either
import dk.rohdef.helperplanning.shifts.ShiftId

interface AxpShiftReferences {
    suspend fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<Unit, ShiftId>
    suspend fun saveAxpBookingToShiftId(bookingNumber: AxpBookingId, shiftId: ShiftId)
    suspend fun shiftIdToAxpBooking(shiftId: ShiftId): Either<Unit, AxpBookingId>
}
