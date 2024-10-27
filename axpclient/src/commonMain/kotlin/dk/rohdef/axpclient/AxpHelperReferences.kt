package dk.rohdef.axpclient

import arrow.core.Either
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId

interface AxpHelperReferences {
    fun all(): List<HelperIdMapping>
    fun helperByTid(tid: HelperTID): Either<Unit, HelperIdMapping>
    fun helperByNumber(number: HelperNumber): Either<Unit, HelperIdMapping>
    fun helperById(helperId: HelperId): Either<Unit, HelperIdMapping>

    fun createHelperReference(tid: HelperTID, number: HelperNumber): Either<Unit, HelperId>
    fun createHelperReference(number: HelperNumber): Either<Unit, HelperId>
}
