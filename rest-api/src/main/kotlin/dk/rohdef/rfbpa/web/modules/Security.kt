package dk.rohdef.rfbpa.web.modules

import com.auth0.jwk.JwkProviderBuilder
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.ktor.ext.inject
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*

fun Application.security() {
    val log = KotlinLogging.logger {}
    val config by inject<RfBpaConfig>()

    install(Authentication) {
        jwt {
            val jwkProvider = JwkProviderBuilder(config.auth.jwkEndpoint).build()

            realm = "rfbpa"
            verifier(jwkProvider, config.auth.jwtIssuer)
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
