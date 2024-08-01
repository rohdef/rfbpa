package dk.rohdef.axpclient

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.right
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.parsing.WeekPlanParser
import dk.rohdef.axpclient.shift.AxpShift
import dk.rohdef.helperplanning.HelpersRepository
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
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

class AxpSalarySystem(
    private val configuration: AxpConfiguration,
    private val axpShiftReferences: AxpShiftReferences,
    private val helperReferences: AxpHelperReferences,
    private val helpersRepository: HelpersRepository,
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
    private val weekPlanParser = WeekPlanParser()

    override suspend fun createShift(
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<Unit, Shift> = either {
        ensureLoggedIn()

        val startInstant = start.localDateTime.toInstant(configuration.timeZone)
        val endInstant = end.localDateTime.toInstant(configuration.timeZone)

        val axpBookingId = axpClient.createShift(startInstant, endInstant, CreateAxpShift.ShiftType.LONG)
            .mapLeft { TODO("Domain error should be added here") }
            .bind()

        val shift = Shift(
            HelperBooking.NoBooking,
            start,
            end,
        )

        axpShiftReferences.saveAxpBookingToShiftId(axpBookingId, shift.shiftId)

        shift
    }

    override suspend fun bookShift(
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<SalarySystemRepository.BookingError, ShiftId> {
        ensureLoggedIn()

        val helperTid = helperReferences.helperById(helperId).axpTid
        val axpBookingId = axpShiftReferences.shiftIdToAxpBooking(shiftId)
            .getOrElse { TODO("Handle the optional better") }
        return axpClient.bookHelper(axpBookingId, helperTid)
            .mapLeft {
                // TODO improve
                log.error { it }
                TODO("Error detected, improve me")
            }
            .map { shiftId }
    }

    internal suspend fun AxpShift.shift(bookingToHelperId: Map<HelperNumber, HelperId>, helpers: Map<HelperId, Helper>): Shift {
        val helperBooking = axpHelperBooking.toHelperBooking(bookingToHelperId, helpers)
        val storedShiftId = axpShiftReferences.axpBookingToShiftId(bookingId)

        val shiftId = when (storedShiftId) {
            is Either.Right -> storedShiftId.value
            is Either.Left -> ShiftId.generateId().apply {
                axpShiftReferences.saveAxpBookingToShiftId(bookingId, this)
            }
        }

        return Shift(
            helperBooking,
            shiftId,
            YearWeekDayAtTime.from(start),
            YearWeekDayAtTime.from(end),
        )
    }

    override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
        ensureLoggedIn()

        val bookingToHelperId: Map<HelperNumber, HelperId> = helperReferences.all()
            .associate { it.axpNumber to it.helperId }
        val helpers: Map<HelperId, Helper> = helpersRepository.all()
            .associate { it.id to it }
        println(bookingToHelperId)
        println(helpers)

        val axpShiftPlan = axpClient.shifts(yearWeek)
        val weekPlan = weekPlanParser.parse(axpShiftPlan.bodyAsText())

        return WeekPlan(
            yearWeek,
            weekPlan.monday.allShifts.map { it.shift(bookingToHelperId, helpers) },
            weekPlan.tuesday.allShifts.map { it.shift(bookingToHelperId, helpers) },
            weekPlan.wednesday.allShifts.map { it.shift(bookingToHelperId, helpers) },
            weekPlan.thursday.allShifts.map { it.shift(bookingToHelperId, helpers) },
            weekPlan.friday.allShifts.map { it.shift(bookingToHelperId, helpers) },
            weekPlan.saturday.allShifts.map { it.shift(bookingToHelperId, helpers) },
            weekPlan.sunday.allShifts.map { it.shift(bookingToHelperId, helpers) },
        ).right()
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
