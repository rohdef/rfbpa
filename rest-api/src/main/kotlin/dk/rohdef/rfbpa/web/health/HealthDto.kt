package dk.rohdef.rfbpa.web.health

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val status: HealthStatus,
    val message: String,
)

@Serializable
enum class HealthStatus {
    HEALTHY,
    UNHEALTHY,
}