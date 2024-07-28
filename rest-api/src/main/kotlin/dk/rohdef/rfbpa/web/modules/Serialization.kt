package dk.rohdef.rfbpa.web.modules

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.serialization() {
    install(AutoHeadResponse)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
}
