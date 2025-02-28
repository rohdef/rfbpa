package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.web.modules.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*

fun main(arguments: Array<String>) = io.ktor.server.netty.EngineMain.main(arguments)

fun Application.main() {
    val log = KotlinLogging.logger {}

    log.info { "Starting web interface" }

    DatabaseConnection.init()

//    install(CallLogging)
    dependencyInjection()
    errorHandling()
    security()
    serialization()
    routes()
}