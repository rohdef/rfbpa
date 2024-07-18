package dk.rohdef.rfbpa.web.persistance.axp

import arrow.core.Either
import arrow.core.singleOrNone
import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import kotlinx.datetime.Clock
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.insert

class DatabaseAxpToDomainmapper(val clock: Clock) : AxpToDomainMapper {
    override suspend fun axpBookingToShiftId(axpBookingId: AxpBookingId): Either<Unit, ShiftId> = dbQuery {
        AxpBookingToShift
            .select(AxpBookingToShift.shiftId)
            .where { AxpBookingToShift.bookingNumber eq axpBookingId.axpId }
            .map { ShiftId(it[AxpBookingToShift.shiftId].toKotlinUUID()) }
            .singleOrNone()
            .toEither { }
    }

    override suspend fun saveAxpBookingToShiftId(bookingNumber: AxpBookingId, shiftId: ShiftId) = dbQuery {
        AxpBookingToShift.insert {
            it[AxpBookingToShift.bookingNumber] = bookingNumber.axpId
            it[AxpBookingToShift.shiftId] = shiftId.id.toJavaUUID()
        }
        Unit
    }

    override suspend fun shiftIdToAxpBooking(shiftId: ShiftId): Either<Unit, AxpBookingId> = dbQuery {
        AxpBookingToShift
            .select(AxpBookingToShift.bookingNumber)
            .where { AxpBookingToShift.shiftId eq shiftId.id.toJavaUUID() }
            .map { AxpBookingId(it[AxpBookingToShift.bookingNumber]) }
            .singleOrNone()
            .toEither { }
    }
}
