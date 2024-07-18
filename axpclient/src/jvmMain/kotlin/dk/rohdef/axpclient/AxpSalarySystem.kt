package dk.rohdef.axpclient

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.right
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.parsing.WeekPlanParser
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.statement.*
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.toInstant
import java.io.Closeable

class AxpSalarySystem(
    private val configuration: AxpConfiguration,
    private val axpToDomainMapper: AxpToDomainMapper,
    private val helpers: AxpRepository,
) : SalarySystemRepository, Closeable {
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
    private val weekPlanParser = WeekPlanParser(helpers, axpToDomainMapper)

    override suspend fun cacheMisses(
        yearWeeks: YearWeekInterval,
        updateStrategy: SalarySystemRepository.UpdateStrategy,
        threshold: DateTimePeriod
    ): Either<Unit, Set<YearWeek>> {
        TODO("not implemented")
    }

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<Unit, Shift> = either {
        ensureLoggedIn()

        val startInstant = start.localDateTime.toInstant(configuration.timeZone)
        val endInstant = end.localDateTime.toInstant(configuration.timeZone)

        val axpBookingId = axpClient.createShift(startInstant, endInstant, AxpShift.ShiftType.LONG)
            .mapLeft { TODO("Domain error should be added here") }
            .bind()


        val shift = Shift(
            HelperBooking.NoBooking,
            start,
            end,
        )

        axpToDomainMapper.saveAxpBookingToShiftId(axpBookingId, shift.shiftId)

        shift
    }

    override suspend fun bookShift(
        shiftId: ShiftId,
        helperId: Helper.ID,
    ): Either<SalarySystemRepository.BookingError, ShiftId> {
        ensureLoggedIn()

        val helperTid = helpers.helperById(helperId).axpTid
        val axpBookingId = axpToDomainMapper.shiftIdToAxpBooking(shiftId)
            .getOrElse { TODO("Handle the optional better") }
        return axpClient.bookHelper(axpBookingId, helperTid)
            .mapLeft {
                // TODO improve
                log.error { it }
                TODO("Error detected, improve me")
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
