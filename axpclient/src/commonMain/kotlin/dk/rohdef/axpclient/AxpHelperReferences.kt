package dk.rohdef.axpclient

import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId

interface AxpHelperReferences {
    fun helperByTid(tid: HelperTID): HelperIdMapping
    fun helperByNumber(number: HelperNumber): HelperIdMapping
    fun helperById(id: HelperId): HelperIdMapping
}
