package dk.rohdef.rfbpa.web.modules

import dk.rohdef.rfbpa.web.calendar.calendar
import dk.rohdef.rfbpa.web.health.health
import dk.rohdef.rfbpa.web.templates.templates
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shifts

fun Application.routes() {
    install(Resources)

    routing {
        authenticate {
            route("/api/public") {
                templates()
                calendar()
                shifts()
            }
        }
        shifts()

        health()
    }
}
