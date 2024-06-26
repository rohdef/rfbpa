package dk.rohdef.rfbpa.configuration

import kotlinx.serialization.Serializable

@Serializable
data class Axp(
    val url: String,
    val username: String,
    val password: String,
)