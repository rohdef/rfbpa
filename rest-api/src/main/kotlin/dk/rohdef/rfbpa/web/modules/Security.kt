package dk.rohdef.rfbpa.web.modules

import arrow.core.*
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.interfaces.Payload
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.helperplanning.RfbpaPrincipal as DomainPrincipal
import dk.rohdef.helperplanning.RfbpaPrincipal.RfbpaRoles
import dk.rohdef.rfbpa.web.ApiError
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Application.security() {
    val log = KotlinLogging.logger {}
    val config by inject<RfBpaConfig>()
    val jwkProvider by inject<JwkProvider>()

    install(Authentication) {
        jwt {
            realm = "rfbpa"
            verifier(jwkProvider, config.auth.jwtIssuer)
            validate {
                // TODO: 19/09/2024 rohdef - deal a bit better with this I think
                fromJwtPayload(it.payload).getOrNull()
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

fun ApplicationCall.rfbpaPrincipal(): Either<ApiError, DomainPrincipal> {
    return principal<RfbpaPrincipal>().toOption()
        .map { it.domainPrincipal }
        .toEither { ApiError.unauthorized("No way!") }
}

@JvmInline
value class RfbpaPrincipal(
    val domainPrincipal: DomainPrincipal
) : Principal

fun fromJwtPayload(payload: Payload): Either<Nothing, RfbpaPrincipal> {
    val realmAccess = payload.getClaim("realm_access").asMap()
    val rolesRaw = realmAccess.get("roles") as List<String>
    val roles = rolesRaw.fold(emptySet<RfbpaRoles>()) { acc, s ->
        when (s.trim()) {
            "shift admin" -> acc + RfbpaRoles.SHIFT_ADMIN
            "template admin" -> acc + RfbpaRoles.TEMPLATE_ADMIN
            else -> acc
        }
    }.toNonEmptySetOrNone()

    return roles
        .toEither { TODO() }
        .flatMap {
            DomainPrincipal(
                DomainPrincipal.Subject(payload.subject),
                DomainPrincipal.Name(payload.getClaim("name").asString()),
                DomainPrincipal.Email(payload.getClaim("email").asString()),
                it,
            ).mapLeft { TODO() }
        }
        .map { RfbpaPrincipal(it) }
}
