package dk.rohdef.axpclient

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.parsing.WeekPlanParser
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.statement.*
import kotlinx.datetime.toInstant
import java.io.Closeable

class AxpWeekPlans(
    private val configuration: AxpConfiguration,
): WeekPlanRepository, Closeable {
    private val log = KotlinLogging.logger { }
    private val client = HttpClient(OkHttp) {
        install(HttpCookies)
//        install(Logging)

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

    override suspend fun createShift(start: YearWeekDayAtTime, end: YearWeekDayAtTime, type: ShiftType): Either<Unit, ShiftId> = either {
        ensureLoggedIn()

        val startInstant = start.localDateTime.toInstant(configuration.timeZone)
        val endInstant = end.localDateTime.toInstant(configuration.timeZone)

        axpClient.createShift(startInstant, endInstant, AxpShift.ShiftType.from(type))
            .mapLeft { TODO("Domain error should be added here") }
            .bind()
    }

    override suspend fun bookShift(
        shiftId: ShiftId,
        helper: HelperBooking.PermanentHelper,
    ): Either<Unit, ShiftId> {
        ensureLoggedIn()

        return axpClient.bookHelper(shiftId, helper)
            .mapLeft {
                // TODO improve
                log.error { it }
            }
            .map { shiftId }
    }

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
