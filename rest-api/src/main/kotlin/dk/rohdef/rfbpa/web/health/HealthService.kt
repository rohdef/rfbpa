package dk.rohdef.rfbpa.web.health

interface HealthService {
    fun healthStatus(): HealthDto
}