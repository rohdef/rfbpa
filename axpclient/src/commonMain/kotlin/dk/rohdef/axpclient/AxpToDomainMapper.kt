package dk.rohdef.axpclient

import arrow.core.Either
import dk.rohdef.helperplanning.shifts.ShiftId

interface AxpToDomainMapper {
    fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<Unit, ShiftId>
    fun saveAxpBookingToShiftId(axpBookingId: AxpBookingId, shiftId: ShiftId)
    fun shiftIdToAxpBooking(shiftId: ShiftId): Either<Unit, AxpBookingId>
}
