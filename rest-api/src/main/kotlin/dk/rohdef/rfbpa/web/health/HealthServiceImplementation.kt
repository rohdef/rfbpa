package dk.rohdef.rfbpa.web.health

class HealthServiceImplementation : HealthService {
    override fun healthStatus(): HealthDto {
        return HealthDto(
            HealthStatus.HEALTHY,
            "I'm ready",
        )
    }
}