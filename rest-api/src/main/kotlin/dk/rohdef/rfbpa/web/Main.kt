package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.web.plugins.dependencyInjection
import dk.rohdef.rfbpa.web.plugins.security
import dk.rohdef.rfbpa.web.plugins.serialization
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val log = KotlinLogging.logger {}

    log.info { "Starting web interface" }

    embeddedServer(Netty, port = 8080) {
//        install(CallLogging)
        dependencyInjection()
        security()
        serialization()

        routing {
            authenticate {
                route("/api/public") {
                    calendar()
                }
            }

            calendar()

            get("/health") {
                call.respondText("I am healthy!")
            }
        }
    }.start(wait = true)
}
