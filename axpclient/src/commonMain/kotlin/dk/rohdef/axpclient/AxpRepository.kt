package dk.rohdef.axpclient

import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.axpclient.helper.Shift
import dk.rohdef.axpclient.helper.Shift.AxpShiftId
import kotlinx.uuid.UUID

interface AxpRepository {
    fun helperByTid(tid: HelperTID): HelperIdMapping
    fun helperByNumber(tid: HelperNumber): HelperIdMapping
    fun helperById(tid: dk.rohdef.helperplanning.helpers.Helper.ID): HelperIdMapping

    fun shiftIdByBookingNumber(axpShiftId: String): UUID {
        TODO()
    }
}
