package dk.rohdef.rfbpa.web.health

import dk.rohdef.arrowktor.get
import dk.rohdef.arrowktor.httpOk
import io.ktor.resources.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.health() {
    val healthService: HealthService by inject()

    get<Health> {
        healthService.healthStatus().httpOk()
    }
}

@Resource("health")
class Health