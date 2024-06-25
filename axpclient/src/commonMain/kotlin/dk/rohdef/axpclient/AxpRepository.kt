package dk.rohdef.axpclient

import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.axpclient.helper.Shift
import dk.rohdef.axpclient.helper.Shift.AxpShiftId
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

interface AxpRepository {
    fun helperByTid(tid: HelperTID): HelperIdMapping
    fun helperByNumber(number: HelperNumber): HelperIdMapping
    fun helperById(id: dk.rohdef.helperplanning.helpers.Helper.ID): HelperIdMapping

    // TODO: 25/06/2024 rohdef - do proper
    fun shiftIdByBookingNumber(axpShiftId: String): UUID {
        return UUID.generateUUID()
    }
}
