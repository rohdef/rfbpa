package dk.rohdef.rfbpa.configuration

import kotlinx.serialization.Serializable

@Serializable
data class RfBpaConfig(
    val client: Client,
    val runtimeMode: RuntimeMode,
)
