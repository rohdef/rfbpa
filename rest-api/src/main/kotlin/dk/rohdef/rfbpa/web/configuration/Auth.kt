package dk.rohdef.rfbpa.web.configuration

import java.net.URL

data class Auth(
    val jwkEndpoint: URL,
    val jwtIssuer: String,
) {
    companion object {
        fun fromMap(auth: Map<String, String>): Auth {
            val jwkEndpoint = auth["jwkEndpoint"]!!
            return Auth(
                URL(jwkEndpoint),
                auth["jwtIssuer"]!!,
            )
        }
    }
}
