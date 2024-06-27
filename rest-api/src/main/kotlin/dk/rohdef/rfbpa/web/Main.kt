package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.web.plugins.dependencyInjection
import dk.rohdef.rfbpa.web.plugins.security
import dk.rohdef.rfbpa.web.plugins.serialization
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(arguments: Array<String>) = io.ktor.server.netty.EngineMain.main(arguments)

fun Application.main() {
    val log = KotlinLogging.logger {}

    log.info { "Starting web interface" }

//    install(CallLogging)
    dependencyInjection()
    security()
    serialization()

    routing {
        authenticate {
            route("/api/public") {
                calendar()
            }
        }

        get("/health") {
            call.respondText("I am healthy!")
        }
    }
}
