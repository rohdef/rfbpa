package dk.rohdef.rfbpa.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val log = KotlinLogging.logger {}
    log.info { "Running web interface" }

    embeddedServer(Netty, port = 8080) {
        install(CORS)
        install(Authentication) {
            basic("calendar") {
                realm = "RF BPA calendar function"
                validate { credentials ->
                    log.info { "checking credentials: ${credentials.name}" }
                    if (credentials.name == "rff" && credentials.password == "rff") {
                        log.info { "Permission granted" }
                        UserIdPrincipal(credentials.name)
                    } else {
                        log.warn { "Rejected credentials" }
                        log.warn { "Rejected credentials" }
                        null
                    }
                }
            }
        }

        routing {
            get("/") {
                call.respondText("Hello, world!")
            }

            calendar()

            get("/health") {
                call.respondText("I am healthy!")
            }
        }
    }.start(wait = true)
}
