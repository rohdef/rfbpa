package dk.rohdef.rfbpa.web

import com.auth0.jwk.Jwk
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.ktor.util.*
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

abstract class RfbpaSpec(body: RfbpaSpec.() -> Unit = {}) : DslDrivenSpec(), FunSpecRootScope {
    val keyPair = KeyPairGenerator.getInstance("RSA")
        .let {
            it.initialize(2048)
            it.genKeyPair()
        }

    val publicKey = keyPair.public as RSAPublicKey
    val privateKey = keyPair.private as RSAPrivateKey

    val jwk: Jwk

    init {
        val encoder = Base64.getUrlEncoder()

        jwk = Jwk(
            "id-10-t",
            "RSA",
            "RS256",
            "",
            listOf(),
            "",
            listOf(),
            "",
            mapOf(
                "n" to encoder.encodeToString(publicKey.modulus.toByteArray()),
                "e" to encoder.encodeToString(publicKey.publicExponent.toByteArray())
            ),
        )

        body()
    }

    val token = JWT.create()
        .withKeyId("id-10-t")

        .withExpiresAt(Date(System.currentTimeMillis() + 1260000))
        .withIssuedAt(Instant.now())
        .withClaim("auth_time", Instant.now().toEpochMilli())
        .withJWTId("some-id-jwt")
        .withIssuer(TestConfiguration.default.auth.jwtIssuer)
        .withAudience("account")
        // TODO: 22/09/2024 rohdef - make testable
        .withSubject("f1kt1vus")
//        .withSubject("aeb2d15c-04e1-415d-9520-ad61ce0a1a6f")
        .withClaim("typ", "Bearer")
        .withClaim("azp", "rfbpa")
        .withClaim("sid", "session-3")
        .withClaim("acr", "1")
        .withArrayClaim("allowed-origins", arrayOf("*"))
        .withClaim(
            "realm_access", mapOf(
                "roles" to listOf(
                    "default-roles-rfbpa",
                    "employer calendar",
                    "template admin",
                    "offline_access",
                    "booking admin",
                    "uma_authorization",
                    "shift admin",
                )
            )
        )
        .withClaim("scope", "openid email profile")
        .withClaim("email_verified", true)
        .withClaim("name", "Rohde Fischer")
        .withClaim("preferred_username", "rohdef@rohdef.dk")
        .withClaim("given_name", "Rohde")
        .withClaim("family_name", "Fischer")
        .withClaim("email", "rohdef@rohdef.dk")
        .sign(Algorithm.RSA256(publicKey, privateKey))

    fun rfbpaTestApplication(
        block: suspend RfBpaSpecScope.(client: HttpClient) -> Unit
    ) {
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    json()
                }

                defaultRequest {
                    headers.appendIfNameAbsent(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            environment {
                // TODO: 24/11/2024 rohdef - probably wanted, where is it after update to ktor 3?
//                developmentMode = false
            }

            RfBpaSpecScope().block(client)
        }
    }

    fun FunSpecRootScope.restTest(
        name: String,
        block: suspend RfBpaSpecScope.(client: HttpClient) -> Unit
    ) = test(name) {
        rfbpaTestApplication(block)
    }

    suspend fun FunSpecContainerScope.restTest(
        name: String,
        block: suspend RfBpaSpecScope.(client: HttpClient) -> Unit,
    ) = test(name) {
        rfbpaTestApplication(block)
    }

    fun FunSpecRootScope.xrestTest(
        name: String,
        block: suspend RfBpaSpecScope.(client: HttpClient) -> Unit
    ) = xtest(name) {
        rfbpaTestApplication(block)
    }

    suspend fun FunSpecContainerScope.xrestTest(
        name: String,
        block: suspend RfBpaSpecScope.(client: HttpClient) -> Unit
    ) = xtest(name) {
        rfbpaTestApplication(block)
    }
}

class RfBpaSpecScope
