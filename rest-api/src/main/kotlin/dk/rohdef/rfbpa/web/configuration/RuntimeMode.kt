package dk.rohdef.rfbpa.configuration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RuntimeMode {
    @SerialName("development") DEVELOPMENT,
    @SerialName("production") PRODUCTION,
}
