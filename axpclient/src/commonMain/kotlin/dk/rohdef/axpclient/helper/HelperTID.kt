package dk.rohdef.axpclient.helper

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.helperplanning.helpers.Helper

data class HelperTID(
    val id: String,
) {
    fun toId(helperRepository: AxpRepository): Helper.ID {
        return helperRepository.helperByTid(this).helperID
    }
}
