package dk.rohdef.axpclient.helper

import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.helperplanning.helpers.Helper

data class HelperTID(
    val id: String,
) {
    fun toId(helperRepository: AxpHelperReferences): Helper.ID {
        return helperRepository.helperByTid(this).helperID
    }
}
