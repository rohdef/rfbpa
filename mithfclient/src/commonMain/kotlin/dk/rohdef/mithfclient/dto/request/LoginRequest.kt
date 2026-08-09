package dk.rohdef.mithfclient.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("u")
    val username: String,

    @SerialName("p")
    val password: String,
) {
    @SerialName("a")
    val action: String = "blogin"

    override fun toString() = "LoginRequest(action=$action, username=$username, password=***)"

}