package dk.rohdef.axpclient

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.helper.AxpMetadataRepository
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.parsing.WeekPlanParser
import dk.rohdef.axpclient.shift.AxpShift
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.SalarySystemRepository
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
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<ShiftsError, Shift> = either {
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
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId,
    ): Either<SalarySystemRepository.BookingError, Unit> = either {
        ensureLoggedIn()

        val helperTid = helperReferences.helperById(helperId)
            .mapLeft {
                log.error { "Could not find helper" }
                TODO()
            }
            .bind()
            .axpTid ?: TODO("Helper does not have a TID, deal with it")
        val axpBookingId = axpShiftReferences.shiftIdToAxpBooking(shiftId)
            .getOrElse { TODO("Handle the optional better") }
        axpClient.bookHelper(axpBookingId, helperTid)
            .mapLeft {
                // TODO improve
                log.error { it }
                TODO("Error detected, improve me")
            }
            .map { shiftId }
            .bind()
    }

    override suspend fun unbookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId
    ): Either<SalarySystemRepository.BookingError, Unit> = either {
        ensureLoggedIn()

        val axpBookingId = axpShiftReferences.shiftIdToAxpBooking(shiftId)
            .getOrElse { TODO("Handle the optional better") }
        axpClient.unbookHelper(axpBookingId)
            .mapLeft {
                // TODO improve
                log.error { it }
                TODO("Error detected, improve me")
            }
            .map { shiftId }
            .bind()
    }

    internal suspend fun AxpShift.shift(): Either<Unit, Shift> = either {
        val bookingToHelperId: (HelperNumber) -> Either<Unit, HelperId> = { number: HelperNumber ->
            val helperId = helperReferences.helperByNumber(number)
                .map { it.helperId }

            when (helperId) {
                is Either.Left -> helperReferences.createHelperReference(number)
                is Either.Right -> helperId
            }
        }
        val vacancyToHelperId: (Any) -> Either<Unit, HelperId> = { TODO() }

        // TODO: 27/10/2024 rohdef - this probably needs a bit of love
        val helperBooking = when (axpHelperBooking) {
            AxpMetadataRepository.NoBooking -> HelperBooking.NoBooking
            is AxpMetadataRepository.PermanentHelper -> {
                val helperId = bookingToHelperId(axpHelperBooking.helperNumber)
                    .bind()
                HelperBooking.Booked(helperId)
            }
            AxpMetadataRepository.VacancyBooking -> {
                val helperId = vacancyToHelperId(TODO())
                    .bind()
                HelperBooking.Booked(helperId)
            }
        }
        val shiftId = axpShiftReferences.axpBookingToShiftId(bookingId)
            .getOrElse {
                ShiftId.generateId().apply {
                    // TODO: 27/10/2024 rohdef - what to do if this fails
                    axpShiftReferences.saveAxpBookingToShiftId(bookingId, this)
                }
            }

        Shift(
            helperBooking,
            shiftId,
            YearWeekDayAtTime.from(start),
            YearWeekDayAtTime.from(end),
        )
    }

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek,
    ): Either<ShiftsError, WeekPlan> = either {
        ensureLoggedIn()

        val axpShiftPlan = axpClient.shifts(yearWeek)
        val weekPlan = weekPlanParser.parse(axpShiftPlan.bodyAsText())

        val axpToDomainShift: suspend (AxpShift) -> Shift = {
            it.shift()
                .mapLeft { TODO() }
                .bind()
        }
        val monday = weekPlan.monday.allShifts.map { axpToDomainShift(it) }
        val tuesday = weekPlan.monday.allShifts.map { axpToDomainShift(it) }
        val wednesday = weekPlan.monday.allShifts.map { axpToDomainShift(it) }
        val thursday = weekPlan.monday.allShifts.map { axpToDomainShift(it) }
        val friday = weekPlan.monday.allShifts.map { axpToDomainShift(it) }
        val saturday = weekPlan.monday.allShifts.map { axpToDomainShift(it) }
        val sunday = weekPlan.monday.allShifts.map { axpToDomainShift(it) }

        WeekPlan(
            yearWeek,
            monday, tuesday, wednesday,
            thursday, friday,
            saturday, sunday,
        )
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
