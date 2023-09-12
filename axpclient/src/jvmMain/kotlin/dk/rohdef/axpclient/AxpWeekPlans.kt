package dk.rohdef.axpclient

import arrow.core.Either
import arrow.core.right
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.parsing.WeekPlanParser
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.helperplanning.shifts.YearWeek
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.statement.*
import mu.KotlinLogging
import java.io.Closeable

private val log = KotlinLogging.logger { }
actual class AxpWeekPlans(
    configuration: AxpConfiguration,
): WeekPlanRepository, Closeable {
    val client = HttpClient(OkHttp) {
        install(HttpCookies)

        engine {
            config {
                followRedirects(false)
            }
        }
    }
    private val axpClient = AxpClient(
        client,
        configuration,
    )
    private val weekPlanParser = WeekPlanParser()

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        ensureLoggedIn()

        val axpShiftPlan = axpClient.shifts(yearWeek)
        val weekPlan = weekPlanParser.parse(axpShiftPlan.bodyAsText())

        return weekPlan.right()
    }

    override fun close() {
        client.close()
    }

    private suspend fun ensureLoggedIn() {
        // TODO this should be handled a lot better
        val cookies = client.cookies("https://www.handicapformidlingen.axp.dk")
        val seessionId = cookies.get("PHPSESSID")

        if (seessionId == null) {
            log.info { "Logging in" }
            axpClient.login()
        }
    }
}