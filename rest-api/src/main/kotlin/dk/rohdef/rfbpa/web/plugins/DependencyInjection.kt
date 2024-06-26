package dk.rohdef.rfbpa.web.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

fun Application.dependencyInjection() {
    val log = KotlinLogging.logger {}

    install(Koin) {

    }
}
