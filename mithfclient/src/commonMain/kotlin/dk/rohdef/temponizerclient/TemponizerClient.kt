package dk.rohdef.temponizerclient

import dk.rohdef.helperplanning.salary_helper.SalaryHelper
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.temponizerclient.authentication.TemponizerCredentials
import dk.rohdef.temponizerclient.configuration.TemponizerConfiguration
import dk.rohdef.temponizerclient.parsing.HelpersParser
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TemponizerClient(
    private val client: HttpClient,
    private val configuration: TemponizerConfiguration,
) {
    private val log = KotlinLogging.logger { }
    private val urls = Urls(
        configuration.temponizer,
        configuration.timeZone,
    )

    suspend fun loginSSO(credentials: TemponizerCredentials) {
        log.info { "Logging in with: $credentials" }

        // Post?
        val loginResult = client.submitForm(
            urls.temponizerLogin,
            parameters {
                append("brugere_id", credentials.userId)
                append("t", credentials.t)
                append("token", credentials.token)
            },
        )

        val headers = loginResult.headers
        log.info { "Status: ${loginResult.status}" }
        log.info { "Headers: $headers" }

        val b = loginResult.bodyAsText()
        log.info { "Body: $b" }

        val cs = client.cookies(urls.temponizer)
        log.info { "Logging in with cookie: $cs" }
    }

    private val helperParser = HelpersParser()
    suspend fun helpers(): List<SalaryHelper> {
        log.info { "Reading helpers" }

        val response = client.request(urls.helpers)
        val body = response.bodyAsText()
        return helperParser.parse(body)
    }

    suspend fun shifts(): List<SalaryShift> {
        log.info { "Reading shifts" }
        TODO()
    }
}