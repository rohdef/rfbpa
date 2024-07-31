package dk.rohdef.axpclient.helper

import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.helperplanning.helpers.HelperId

data class HelperTID(
    val id: String,
) {
    fun toId(helperRepository: AxpHelperReferences): HelperId {
        return helperRepository.helperByTid(this).helperID
    }
}
