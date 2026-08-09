package dk.rohdef.mithfclient.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class HelpersRequest(
    val csrf: String,
    val eid: String,
) {
    val a = "pulje"
}