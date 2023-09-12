package dk.rohdef.axpclient

import arrow.core.None
import arrow.core.Some
import arrow.core.toOption
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.shifts.YearWeek
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

internal class AxpClient(
    private val client: HttpClient,
    private val configuration: AxpConfiguration,
) {
    private val urls = AxpUrls(configuration.url)

    suspend fun login(): LoginResult {
        log.info { "Logging in with user ${configuration.username}" }
        val loginResult = client.submitForm(
            urls.login,
            parameters {
                append("username", configuration.username)
                append("password", configuration.password)
                append("login", "hmm")
            },
        )

        val headers = loginResult.headers
        val locationHeader = headers.get("location")
            .toOption()

        return when (locationHeader) {
            is Some -> when (locationHeader.value) {
                urls.index -> LoginResult.Success
                urls.indexUWeb -> LoginResult.Success
                urls.login -> LoginResult.NotAuthorized
                else -> LoginResult.InvalidResult("Unrecognised redirect location for AXP login: ${locationHeader.value}")
            }

            None -> LoginResult.InvalidResult("Location header is missing in the login response from AXP")
            else -> {
                TODO()
            }
        }
    }

    suspend fun shifts(yearWeek: YearWeek): HttpResponse {
        val shiftsUrl = urls.shiftsForWeek(yearWeek)
        log.debug { shiftsUrl }
        return client.request(shiftsUrl)
    }
}