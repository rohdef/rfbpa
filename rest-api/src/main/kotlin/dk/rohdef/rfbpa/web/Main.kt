package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.web.modules.dependencyInjection
import dk.rohdef.rfbpa.web.modules.routes
import dk.rohdef.rfbpa.web.modules.security
import dk.rohdef.rfbpa.web.modules.serialization
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.resources.*

fun main(arguments: Array<String>) = io.ktor.server.netty.EngineMain.main(arguments)

fun Application.main() {
    val log = KotlinLogging.logger{}

    log.info { "Starting web interface" }

    DatabaseConnection.init()

    // TODO: 12/10/2024 rohdef - refactor this to a module, perhaps also something for monitoring
    install(Resources)

//    install(CallLogging)
    dependencyInjection()
    security()
    serialization()
    routes()
}
