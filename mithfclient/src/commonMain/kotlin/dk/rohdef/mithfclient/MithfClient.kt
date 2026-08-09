package dk.rohdef.mithfclient

import dk.rohdef.mithfclient.authentication.Btok
import dk.rohdef.mithfclient.authentication.Credentials
import dk.rohdef.mithfclient.configuration.MithfConfiguration
import dk.rohdef.mithfclient.dto.request.LoginRequest
import dk.rohdef.mithfclient.dto.response.LoginResponse
import dk.rohdef.temponizerclient.authentication.TemponizerCredentials
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class MithfClient(
    private val client: HttpClient,
    private val configuration: MithfConfiguration,
) {
    private val parser = MithfParser()
    private val log = KotlinLogging.logger { }
    private val urls = Urls(
        configuration.mithf,
        configuration.timeZone,
    )

    suspend fun login(credentials: Credentials): Btok {
        log.info { "Performing login for: ${credentials.username}" }

        val login = LoginRequest(
            credentials.username,
            credentials.password,
        )

        val response = client.post(urls.mitHfAPI) {
            contentType(ContentType.Application.Json)
            setBody(login)
        }

        log.info { "Login: ${response.status}" }
        val body: LoginResponse = response.body()
        log.info { "Response: $body" }
        return Btok(body.token)
    }

    suspend fun temponizerCredentials(btok: Btok): TemponizerCredentials {
        log.info { "Getting temporary credentials" }

        // Post?
        val loginResult = client.submitForm(
            urls.mithfLogin,
            parameters {
                append("btok", btok.token)
            },
        )

        val resp = loginResult.bodyAsText()

        return parser.parseSsoCredentials(resp)
    }

    suspend fun helpers(btok: Btok): List<String> {


        TODO()
    }
}