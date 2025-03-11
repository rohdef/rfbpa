package dk.rohdef.axpclient

import arrow.core.Either
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId

interface AxpHelperReferences {
    suspend fun all(): List<HelperIdMapping>
    suspend fun helperByTid(tid: HelperTID): Either<Unit, HelperIdMapping>
    suspend fun helperByNumber(number: HelperNumber): Either<Unit, HelperIdMapping>
    suspend fun helperById(id: HelperId): Either<Unit, HelperIdMapping>

    suspend fun createHelperReference(tid: HelperTID, number: HelperNumber): Either<Unit, HelperId>
    suspend fun createHelperReference(number: HelperNumber): Either<Unit, HelperId>
}
