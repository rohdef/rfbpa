package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.helperplanning.shifts.ShiftId
import kotlinx.datetime.Clock

class DatabaseAxpToDomainmapper(val clock: Clock): AxpToDomainMapper {
    private val _map = mutableMapOf<AxpBookingId, ShiftId>()

    override fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<Unit, ShiftId> {
        // TODO: 18/07/2024 rohdef - bad implementation
        return _map[axpBookingId]?.let { return it.right() } ?: Unit.left()
    }

    override fun saveAxpBookingToShiftId(axpBookingId: AxpBookingId, shiftId: ShiftId) {
        _map[axpBookingId] = shiftId
    }

    override fun shiftIdToAxpBooking(shiftId: ShiftId): Either<Unit, AxpBookingId> {
        TODO("not implemented")
    }
}
