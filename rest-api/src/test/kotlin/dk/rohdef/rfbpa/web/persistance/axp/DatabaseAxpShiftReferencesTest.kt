package dk.rohdef.rfbpa.web.persistance.axp

import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.AxpShiftReferences
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfbpa.web.persistance.TestDatabaseConnection
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec

class DatabaseAxpShiftReferencesTest : FunSpec({
    val shiftReferenceRepository: AxpShiftReferences = DatabaseAxpShiftReferences()

    beforeEach {
        TestDatabaseConnection.init()
    }

    afterEach {
        TestDatabaseConnection.disconnect()
    }

    test("creating and reading shift references") {
        val bookingId = AxpBookingId("ID-10-T")
        val shiftId = ShiftId.generateId()

        shiftReferenceRepository.axpBookingToShiftId(bookingId) shouldBeLeft AxpShiftReferences.ShiftIdNotFound(bookingId)
        shiftReferenceRepository.shiftIdToAxpBooking(shiftId) shouldBeLeft AxpShiftReferences.BookingIdNotFound(shiftId)

        shiftReferenceRepository.saveAxpBookingToShiftId(bookingId, shiftId)

        shiftReferenceRepository.axpBookingToShiftId(bookingId) shouldBeRight shiftId
        shiftReferenceRepository.shiftIdToAxpBooking(shiftId) shouldBeRight bookingId
    }
})
