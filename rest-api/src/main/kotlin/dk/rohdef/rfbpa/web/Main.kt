package dk.rohdef.rfbpa.web

import arrow.core.Either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.rfbpa.web.modules.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.inject

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

    val seeder by inject<Seeder>()
    runBlocking {
        val helpers = seeder.seedHelpers()

        when (helpers) {
            is Either.Left<Seeder.SeedError> -> {
                log.error { "Failed to seed helpers, got: ${helpers.value}" }
            }
            is Either.Right<List<Helper>> -> {
                log.info { "Successfully seeded helpers" }
                log.info { helpers.value.joinToString(",\n") { it.toString() } }
            }
        }
    }
}