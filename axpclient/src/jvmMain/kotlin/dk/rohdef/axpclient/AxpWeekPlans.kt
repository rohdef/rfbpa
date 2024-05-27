package dk.rohdef.axpclient

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.parsing.WeekPlanParser
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.statement.*
import kotlinx.datetime.Instant
import java.io.Closeable

class AxpWeekPlans(
    configuration: AxpConfiguration,
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

    override suspend fun createShift(start: Instant, end: Instant, type: ShiftType): Either<Unit, BookingId> = either {
        ensureLoggedIn()

        axpClient.createShift(AxpShift.CustomerId("1366"), start, end, AxpShift.ShiftType.from(type))
            .mapLeft { TODO("Domain error should be added here") }
            .bind()
    }

    override suspend fun bookShift(
        helper: HelperBooking,
        type: ShiftType,
        start: Instant,
        end: Instant,
    ): Either<Unit, BookingId> {
        ensureLoggedIn()

        val shift = AxpShift(
            start,
            end,
            helper,
            AxpShift.ShiftType.from(type),
            // TODO: 23/04/2024 rohdef - where does this customer ID come from? Do we need?
            AxpShift.CustomerId("1366"),
        )

        return axpClient.bookShift(shift)
            .mapLeft {
                // TODO improve
                log.error { it }
            }
    }

    suspend fun registrate(

    ): Either<Unit, Unit> {
        TODO()
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
