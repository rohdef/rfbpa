package dk.rohdef.rfbpa.configuration

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val axp: Axp,
)
