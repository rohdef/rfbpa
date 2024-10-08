package dk.rohdef.rfbpa.web.modules

import dk.rohdef.rfbpa.web.calendar.calendar
import dk.rohdef.rfbpa.web.templates.templates
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shifts

fun Application.routes() {
    routing {
        templates()
        authenticate {
            route("/api/public") {
                calendar()
                shifts()
            }
        }

        get("/health") {
            call.respondText("I am healthy!")
        }
    }
}
