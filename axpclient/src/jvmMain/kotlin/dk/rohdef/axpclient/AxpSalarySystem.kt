package dk.rohdef.axpclient

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.right
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.helper.AxpHelperBooking
import dk.rohdef.axpclient.helper.AxpIllnessBooking
import dk.rohdef.axpclient.parsing.WeekPlanParser
import dk.rohdef.axpclient.shift.AxpIllnessShift
import dk.rohdef.axpclient.shift.AxpShift
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.salary_shifts.SalaryBooking
import dk.rohdef.helperplanning.salary_shifts.SalaryRegistration
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.helperplanning.salary_shifts.SalaryWeekPlan
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
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
    private val client = HttpClient(CIO) {
        install(HttpCookies)
//        install(Logging)
    }
    private val axpClient = AxpClient(
        client,
        configuration,
    )
    private val weekPlanParser = WeekPlanParser()

    override suspend fun reportIllness(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
        replacementShiftId: ShiftId
    ): Either<SalarySystemRepository.RegisterIllnessError, Unit> = either {
        ensureLoggedIn()

        val axpBookingId = axpShiftReferences.shiftIdToAxpBooking(shift.shiftId)
            .getOrElse { TODO("Handle the optional better") }

        axpClient.reportIllness(
            axpBookingId,
            shift.start.date,
        )
    }

    override suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime,
    ): Either<ShiftsError, SalaryShift> = either {
        ensureLoggedIn()

        val startInstant = start.localDateTime.toInstant(configuration.timeZone)
        val endInstant = end.localDateTime.toInstant(configuration.timeZone)

        val axpBookingId = axpClient.createShift(startInstant, endInstant, CreateAxpShift.ShiftType.LONG)
            .mapLeft { TODO("Domain error should be added here") }
            .bind()

        val shift = SalaryShift(
            SalaryBooking.NoBooking,
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
                log.error { "Could not find helper ${helperId}" }
                SalarySystemRepository.BookingError.HelperNotFound(helperId)
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

    internal suspend fun AxpShift.shift(): Either<ShiftMappingError, SalaryShift> = either {
        val shiftId = axpShiftReferences.axpBookingToShiftId(bookingId)
            .getOrElse {
                ShiftId.generateId().apply {
                    axpShiftReferences.saveAxpBookingToShiftId(bookingId, this)
                        .mapLeft { ShiftMappingError.UnknownError }
                        .bind()
                }
            }

        val helperBooking = axpHelperBooking.toHelperBooking()
            .mapLeft { ShiftMappingError.UnknownError }
            .bind()

        SalaryShift(
            helperBooking,
            shiftId,
            YearWeekDayAtTime.from(start),
            YearWeekDayAtTime.from(end),
        )
    }

    internal suspend fun AxpIllnessShift.shift(): Either<ShiftMappingError, SalaryShift> = either {
        val shiftId = axpShiftReferences.axpBookingToShiftId(bookingId)
            .getOrElse {
                ShiftId.generateId().apply {
                    axpShiftReferences.saveAxpBookingToShiftId(bookingId, this)
                        .mapLeft { ShiftMappingError.UnknownError }
                        .bind()
                }
            }

        val helperBooking = axpHelperBooking.toHelperBooking()
            .mapLeft { ShiftMappingError.UnknownError }
            .bind()

        SalaryShift(
            helperBooking,
            shiftId,
            YearWeekDayAtTime.from(start),
            YearWeekDayAtTime.from(end),
            listOf(
                SalaryRegistration.Illness
            ),
        )
    }

    private suspend fun AxpHelperBooking.toHelperBooking(): Either<Unit, SalaryBooking> {
        return when (this) {
            AxpHelperBooking.NoBooking -> SalaryBooking.NoBooking.right()
            is AxpHelperBooking.PermanentHelper -> toHelperBooking()
            is AxpHelperBooking.VacancyBooking -> SalaryBooking.NoBooking.right()
        }
    }

    private suspend fun AxpHelperBooking.PermanentHelper.toHelperBooking(): Either<Unit, SalaryBooking> {
        val helperReference = helperReferences.helperByNumber(helperNumber)
            .map { SalaryBooking.Helper(it.helperId) }

        return when (helperReference) {
            is Either.Left -> TODO("This will not work, either find a way to resync, look up helper or similar or this has to be an error")
            is Either.Right -> helperReference
        }
    }

    private suspend fun AxpIllnessBooking.toHelperBooking(): Either<Unit, SalaryBooking> {
        return when (this) {
            is AxpIllnessBooking.PermanentHelper -> toHelperBooking()
            AxpIllnessBooking.VacancyBooking -> TODO("Dealing with illness and vacancies is currently not supported")
        }
    }

    private suspend fun AxpIllnessBooking.PermanentHelper.toHelperBooking(): Either<Unit, SalaryBooking> {
        val helperReference = helperReferences.helperByTid(helperTid)
            .map { SalaryBooking.Helper(it.helperId) }

        return when (helperReference) {
            is Either.Left -> TODO("We probably want to send a domain error now - either that or find the desired number")
            is Either.Right -> helperReference
        }
    }

    sealed interface ShiftMappingError {
        // TODO placeholder until further error handling in possible #59
        object UnknownError : ShiftMappingError
    }

    suspend fun illnessBooking(
        bookingNumber: AxpBookingId
    ): AxpIllnessBooking {
        ensureLoggedIn()

        val helpersForShift = axpClient.helperForShift(bookingNumber)
        return weekPlanParser.illnessBooking(helpersForShift.bodyAsText())
    }

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek,
    ): Either<ShiftsError, SalaryWeekPlan> = either {
        ensureLoggedIn()

        val axpShiftPlan = axpClient.shifts(yearWeek)
        val weekPlan = weekPlanParser.parse(axpShiftPlan.bodyAsText(), this@AxpSalarySystem::illnessBooking)

        val axpToDomainShift: suspend (AxpShift) -> SalaryShift = {
            it.shift()
                .mapLeft { TODO() }
                .bind()
        }
        val axpToDomainShiftI: suspend (AxpIllnessShift) -> SalaryShift = {
            it.shift()
                .mapLeft { TODO() }
                .bind()
        }

        val mondayNormal = weekPlan.monday.allShifts.map { axpToDomainShift(it) }
        val mondayIllness = weekPlan.monday.illness.map { axpToDomainShiftI(it) }
        val monday = mondayNormal + mondayIllness

        val tuesdayNormal = weekPlan.tuesday.allShifts.map { axpToDomainShift(it) }
        val tuesdayIllness = weekPlan.tuesday.illness.map { axpToDomainShiftI(it) }
        val tuesday = tuesdayNormal + tuesdayIllness

        val wednesdayNormal = weekPlan.wednesday.allShifts.map { axpToDomainShift(it) }
        val wednesdayIllness = weekPlan.wednesday.illness.map { axpToDomainShiftI(it) }
        val wednesday = wednesdayNormal + wednesdayIllness

        val thursdayNormal = weekPlan.thursday.allShifts.map { axpToDomainShift(it) }
        val thursdayIllness = weekPlan.thursday.illness.map { axpToDomainShiftI(it) }
        val thursday = thursdayNormal + thursdayIllness

        val fridayNormal = weekPlan.friday.allShifts.map { axpToDomainShift(it) }
        val fridayIllness = weekPlan.friday.illness.map { axpToDomainShiftI(it) }
        val friday = fridayNormal + fridayIllness

        val saturdayNormal = weekPlan.saturday.allShifts.map { axpToDomainShift(it) }
        val saturdayIllness = weekPlan.saturday.illness.map { axpToDomainShiftI(it) }
        val saturday = saturdayNormal + saturdayIllness

        val sundayNormal = weekPlan.sunday.allShifts.map { axpToDomainShift(it) }
        val sundayIllness = weekPlan.sunday.illness.map { axpToDomainShiftI(it) }
        val sunday = sundayNormal + sundayIllness

        SalaryWeekPlan(
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