package dk.rohdef.axpclient

import arrow.core.*
import arrow.core.continuations.either
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.shifts.BookingId
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.YearWeek
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    suspend fun bookShift(shift: AxpShift): Either<BookShiftError, BookingId> = either {
        when (shift.helper) {
            HelperBooking.NoBooking -> {}
            is HelperBooking.PermanentHelper -> {
                checkApprovedConflict(shift).bind()
                checkDoubleConflict(shift).bind()
            }

            is HelperBooking.VacancyHelper -> TODO()
        }

        checkCustomerContract(shift).bind()
        val bookingId = saveShift(shift).bind()
        saveBooking(shift, bookingId).bind()

        bookingId.bookingId()
    }

    private enum class ShiftPlanAct {
        CHECK_APPROVED_CONFLICT,
        CHECK_DOUBLE_CONFLICT,
        CHECK_CUSTOMER_CONTRACT,
        SAVE_SHIFT,
        SAVE_BOOKING,
    }

    private fun ParametersBuilder.shiftPlan(shiftPlanAct: ShiftPlanAct, customerId: AxpShift.CustomerId) {
        val subAct = shiftPlanAct.name.lowercase()

        append("act", "shift_plan")
        append("sub_act", subAct)
        // TODO check if it breaks 4th call
        append("axp_act", "cust_shift_plan")

        append("axp_cust_id", customerId.id)

        append("getting_popped", "1")
        append("axp_silent_run", "1")
        // probably needed
        append("quiet_run", "1")
    }

    private fun ParametersBuilder.shiftTimes(start: Instant, end: Instant) {
        start
            .let { DDate(it) }
            .let { Json.encodeToString(it) }
            .let {
                append("from_date", it)
                append("to_date", it)
                append("date", it)
            }
        start.toLocalDateTime(TimeZone.of("Europe/Copenhagen"))
            .let { "${it.hour}:${it.minute}" }
            .let { append("from_time", it) }
        end.toLocalDateTime(TimeZone.of("Europe/Copenhagen"))
            .let { "${it.hour}:${it.minute}" }
            .let { append("to_time", it) }
    }

    private fun ParametersBuilder.doNotRepeat() {
        append("repeat", "0")
        append("repeat_each", "day")
        // TODO inclusive or exclusive?
        append("repeat_to", "{\"year\":\"\",\"month\":\"\",\"day\":\"\"}")
    }

    private fun ParametersBuilder.forHelper(helper: HelperBooking) {
        when (helper) {
            HelperBooking.NoBooking -> TODO()
            is HelperBooking.PermanentHelper -> append("axp_tid", helper.axpId)
            is HelperBooking.VacancyHelper -> TODO()
        }
    }

    private suspend fun checkApprovedConflict(shift: AxpShift): Either<BookShiftError, Unit> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.CHECK_APPROVED_CONFLICT, shift.customerId)
                forHelper(shift.helper)

                append("shifttype", shift.type.axpId)
                shiftTimes(shift.start, shift.end)
                doNotRepeat()

                append("booking", "")
                append("bizarea", "34")
            }
        )
        val body: String = response.body()
        log.warn { "checkApprovedConflict: [$body]" }

        return when (body) {
            "" -> Unit.right()
            else -> BookShiftError.UnknownError(response, body).left()
        }
    }

    private suspend fun checkDoubleConflict(shift: AxpShift): Either<BookShiftError, Unit> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.CHECK_DOUBLE_CONFLICT, shift.customerId)
                forHelper(shift.helper)

                append("shifttype", shift.type.axpId)
                shiftTimes(shift.start, shift.end)
                doNotRepeat()

                append("booking", "")
                append("bizarea", "34")
            }
        )
        val body: String = response.body()
        log.warn { "checkDoubleConflict: [$body]" }

        return when (body) {
            "" -> Unit.right()
            "1" -> BookShiftError.DoubleConflict.left()
            else -> BookShiftError.UnknownError(response, body).left()
        }
    }

    private suspend fun checkCustomerContract(shift: AxpShift): Either<BookShiftError, Unit> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.CHECK_CUSTOMER_CONTRACT, shift.customerId)

                append("from", shift.start.epochSeconds.toString())
                append("to", shift.end.epochSeconds.toString())

                append("bizarea", "34")
            }
        )

        val body = response.body<String>()
        log.warn { "checkCustomerContract: [$body]" }

        return when (body) {
            "1" -> Unit.right()
            else -> {
                BookShiftError.UnknownError(response, body).left()
            }
        }
    }

    private suspend fun saveShift(shift: AxpShift): Either<BookShiftError, AxpBookingId> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.SAVE_SHIFT, shift.customerId)

                append("shifttype", shift.type.axpId)
                shiftTimes(shift.start, shift.end)
                doNotRepeat()

                append("booking", "")
                append("bizarea", "34")
            }
        )
        val body: String = response.body()
        log.warn { "saveShift: [$body]" }

        return when {
            body.lowercase().startsWith("ok") ->
                AxpBookingId(body.substring(2))
                    .right()

            else ->
                BookShiftError.SaveShift
                    .left()
        }
    }

    private suspend fun saveBooking(shift: AxpShift, bookingId: AxpBookingId): Either<BookShiftError, Unit> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.SAVE_BOOKING, shift.customerId)

                append("booking", bookingId.axpId)
                // TODO deal with helper in a good way
                append("book_temp", (shift.helper as HelperBooking.PermanentHelper).axpId)
            }
        )
        val body: String = response.body()
        log.warn { "saveBooking: [$body]" }

        return Unit.right()
    }

    @Serializable
    data class DDate(
        val year: String,
        val month: String,
        val day: String,
    ) {
        constructor(instant: Instant) :
                this(instant.toLocalDateTime(TimeZone.of("Europe/Copenhagen")))

        private constructor(date: LocalDateTime) :
                this(
                    "${date.year}",
                    date.monthNumber.toString().padStart(2, '0'),
                    date.dayOfMonth.toString().padStart(2, '0'),
                )
    }
}

sealed interface BookShiftError {
    data class UnknownError(
        val response: HttpResponse,
        val body: String,
    ) : BookShiftError

    data object ApprovedConflict : BookShiftError

    data object DoubleConflict : BookShiftError

    data object CustomerContract : BookShiftError

    data object SaveShift : BookShiftError

    data object SaveBooking : BookShiftError
}