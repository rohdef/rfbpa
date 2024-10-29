package dk.rohdef.axpclient

import arrow.core.*
import arrow.core.raise.either
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.rfweeks.YearWeek
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val log = KotlinLogging.logger { }

internal class AxpClient(
    private val client: HttpClient,
    private val configuration: AxpConfiguration,
) {
    private val customerId = CreateAxpShift.CustomerId("1366")

    private val urls = AxpUrls(configuration.url, configuration.timeZone)

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
        }
    }

    suspend fun shifts(yearWeek: YearWeek): HttpResponse {
        val shiftsUrl = urls.shiftsForWeek(yearWeek)
        log.debug { shiftsUrl }
        return client.request(shiftsUrl)
    }

    suspend fun createShift(
        start: Instant,
        end: Instant,
        type: CreateAxpShift.ShiftType,
    ): Either<BookShiftError, AxpBookingId> = either {
        log.info { "Creating shift" }

        checkCustomerContract(start, end).bind()
        saveShift(start, end, type).bind()
    }

    suspend fun bookHelper(
        booking: AxpBookingId,
        helper: HelperTID,
    ): Either<BookShiftError, AxpBookingId> = either {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.SAVE_BOOKING)

                append("booking", booking.axpId)
                append("book_temp", helper.id)
            }
        )
        val body: String = response.body()
        log.debug { "saveBooking: [$body]" }

        booking
    }

    suspend fun unbookHelper(
        booking: AxpBookingId,
    ): Either<BookShiftError, AxpBookingId> = either {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.SAVE_BOOKING)

                append("booking", booking.axpId)
                append("clear_temp", "1")
            }
        )
        val body: String = response.body()
        log.debug { "saveBooking: [$body]" }

        booking
    }

    private enum class ShiftPlanAct {
        CHECK_APPROVED_CONFLICT,
        CHECK_DOUBLE_CONFLICT,
        CHECK_CUSTOMER_CONTRACT,
        SAVE_SHIFT,
        SAVE_BOOKING,
        SHIFT_REGISTRATIONS,
    }

    private fun ParametersBuilder.shiftPlan(shiftPlanAct: ShiftPlanAct) {
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

    private fun ParametersBuilder.shiftTimes(start: Instant, end: Instant, type: CreateAxpShift.ShiftType) {
        start
            .let { DDate(it, configuration.timeZone) }
            .let { Json.encodeToString(it) }
            .let {
                append("from_date", it)
                append("to_date", it)
                append("date", it)
            }
        start.toLocalDateTime(configuration.timeZone)
            .let { "${it.hour}:${it.minute}" }
            .let { append("from_time", it) }
        end.toLocalDateTime(configuration.timeZone)
            .let { "${it.hour}:${it.minute}" }
            .let { append("to_time", it) }

        append("shifttype", type.axpId)
    }

    private fun ParametersBuilder.doNotRepeat() {
        append("repeat", "0")
        append("repeat_each", "day")
        // TODO inclusive or exclusive?
        append("repeat_to", "{\"year\":\"\",\"month\":\"\",\"day\":\"\"}")
    }

    private fun ParametersBuilder.forHelper(helper: HelperTID) {
        append("axp_tid", helper.id)
    }

    private suspend fun checkCustomerContract(
        start: Instant,
        end: Instant,
    ): Either<BookShiftError, Unit> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.CHECK_CUSTOMER_CONTRACT)

                append("from", start.epochSeconds.toString())
                append("to", end.epochSeconds.toString())

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

    private suspend fun saveShift(
        start: Instant,
        end: Instant,
        type: CreateAxpShift.ShiftType,
    ): Either<BookShiftError, AxpBookingId> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.SAVE_SHIFT)

                shiftTimes(start, end, type)
                doNotRepeat()

                append("booking", "")
                append("bizarea", "34")
            }
        )
        val body: String = response.body()
        log.debug { "saveShift: [$body]" }

        return when {
            body.lowercase().startsWith("ok") ->
                AxpBookingId(body.substring(2))
                    .right()

            else ->
                BookShiftError.SaveShift
                    .left()
        }
    }

    private suspend fun clearBooking(
        bookingId: AxpBookingId
    ): Either<BookShiftError, Unit> {
        val response = client.submitForm(
            urls.index,
            parameters {
                shiftPlan(ShiftPlanAct.SAVE_BOOKING)

                append("booking", bookingId.axpId)
                append("clear_temp", "1")
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
        constructor(instant: Instant, timeZone: TimeZone) :
                this(instant.toLocalDateTime(timeZone))

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
