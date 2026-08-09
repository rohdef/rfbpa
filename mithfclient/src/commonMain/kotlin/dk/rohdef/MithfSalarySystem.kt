package dk.rohdef

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.salary_helper.SalaryHelper
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.helperplanning.salary_shifts.SalaryWeekPlan
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.mithfclient.MithfClient
import dk.rohdef.mithfclient.authentication.Btok
import dk.rohdef.mithfclient.authentication.Credentials
import dk.rohdef.mithfclient.configuration.MithfConfiguration
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.temponizerclient.TemponizerClient
import dk.rohdef.temponizerclient.configuration.TemponizerConfiguration
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class MithfSalarySystem(
    mithfConfiguration: MithfConfiguration,
    temponizerConfiguration: TemponizerConfiguration,
) : SalarySystemRepository {
    // TODO handle crendtials properly
    private var credentials: Credentials? = null
    private var btok: Btok? = null

    private val client = HttpClient(CIO) {
        install(HttpCookies)
        install(Logging)

        install(DefaultRequest) {
            // default url - add this soon
//            url("https://ktor.io/docs/")

            header(HttpHeaders.AcceptLanguage, "Gallifreyan")
        }

        install(UserAgent) {
            agent = "RfBpa v0.0.1"
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(5)
            retryOnExceptionIf(5) { _, cause -> cause is HttpRequestTimeoutException }
            retryOnExceptionIf(5) { _, cause -> cause is ConnectTimeoutException }
            retryOnExceptionIf(5) { _, cause -> cause is SocketTimeoutException }
            exponentialDelay()

            modifyRequest { request ->
                request.headers.append("x-retry-count", retryCount.toString())
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 2000
            connectTimeoutMillis = 500
            socketTimeoutMillis = 500
        }

        install(ContentNegotiation) {
            json()
        }
    }
    private val mithfClient = MithfClient(
        client,
        mithfConfiguration,
    )
    private val temponizerClient = TemponizerClient(
        client,
        temponizerConfiguration,
    )

    fun registerCredentials(
        subject: RfbpaPrincipal.Subject,
        credentials: Credentials,
    ) {
        this.credentials = credentials
    }

    private suspend fun ensureLoggedIn(
        subject: RfbpaPrincipal.Subject,
    ) {
        if (btok == null) {
            btok = mithfClient.login(credentials!!)
        }

        val temponizerCredentials = mithfClient.temponizerCredentials(btok!!)
        temponizerClient.loginSSO(temponizerCredentials)
    }

    override suspend fun helpers(subject: RfbpaPrincipal.Subject): Either<Unit, List<SalaryHelper>> = either {
        ensureLoggedIn(subject)
        temponizerClient.helpers()
    }

    override suspend fun bookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId,
        helperId: HelperId
    ): Either<SalarySystemRepository.BookingError, Unit> {
        ensureLoggedIn(subject)
        TODO("Not yet implemented")
    }

    override suspend fun reportIllness(
        subject: RfbpaPrincipal.Subject,
        shift: Shift,
        replacementShiftId: ShiftId
    ): Either<SalarySystemRepository.RegisterIllnessError, Unit> {
        ensureLoggedIn(subject)
        TODO("Not yet implemented")
    }

    override suspend fun unbookShift(
        subject: RfbpaPrincipal.Subject,
        shiftId: ShiftId
    ): Either<SalarySystemRepository.BookingError, Unit> {
        ensureLoggedIn(subject)
        TODO("Not yet implemented")
    }

    override suspend fun shifts(
        subject: RfbpaPrincipal.Subject,
        yearWeek: YearWeek
    ): Either<ShiftsError, SalaryWeekPlan> {
        ensureLoggedIn(subject)



        TODO("Not yet implemented")
    }

    override suspend fun createShift(
        subject: RfbpaPrincipal.Subject,
        start: YearWeekDayAtTime,
        end: YearWeekDayAtTime
    ): Either<ShiftsError, SalaryShift> {
        ensureLoggedIn(subject)
        TODO("Not yet implemented")
    }
}