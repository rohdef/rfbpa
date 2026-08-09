package dk.rohdef.mithfclient.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val ok: Boolean,
    val token: String,

    @SerialName("navn")
    val firstName: String,

    @SerialName("skift")
    val skift: Int,
)
