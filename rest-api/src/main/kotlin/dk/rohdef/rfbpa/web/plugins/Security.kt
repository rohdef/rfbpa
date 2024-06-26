package dk.rohdef.rfbpa.web.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*

fun Application.security() {
    val log = KotlinLogging.logger {}

    install(Authentication) {
        jwt {
            val jwkEndpointUrl = URLBuilder(
                "http://localhost:8383/realms/rfbpa/protocol/openid-connect/certs"
            ).build().toURI().toURL()
            val jwkProvider = JwkProviderBuilder(jwkEndpointUrl).build()

            realm = "rfbpa"
            verifier(jwkProvider, "http://localhost:8383/realms/rfbpa")
            validate { jwtCredential ->
                JWTPrincipal(jwtCredential.payload)
//                    if (jwtCredential.payload.issuer != null) {
//                    } else {
//                        null
//                    }
            }
            challenge { defaultScheme, realm ->
                log.error { "Token is not valid or has expired" }
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AccessControlAllowCredentials)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
    }
}
