package dk.rohdef.axpclient

import arrow.core.Either
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId

interface AxpHelperReferences {
    suspend fun bookings(): List<HelperIdMapping>
    suspend fun helperById(id: HelperId): Either<FindIdMappingError.HelperIdNotFound, HelperIdMapping>
    suspend fun helperByNumber(number: HelperNumber): Either<FindIdMappingError.HelperNumberNotFound, HelperIdMapping>
    suspend fun helperByTid(tid: HelperTID): Either<FindIdMappingError.HelperTidNotFound, HelperIdMapping>

    suspend fun createHelperReference(number: HelperNumber, tid: HelperTID, id: HelperId): Either<Unit, HelperId>
    suspend fun createHelperReference(number: HelperNumber, id: HelperId): Either<Unit, HelperId>

    sealed interface FindIdMappingError {
        data class HelperIdNotFound(val id: HelperId) : FindIdMappingError
        data class HelperNumberNotFound(val number: HelperNumber) : FindIdMappingError
        data class HelperTidNotFound(val tid: HelperTID) : FindIdMappingError
    }
}
