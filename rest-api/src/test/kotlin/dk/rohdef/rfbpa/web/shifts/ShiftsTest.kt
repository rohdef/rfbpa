package dk.rohdef.rfbpa.web.shifts

import com.auth0.jwk.JwkProvider
import dk.rohdef.helperplanning.MemoryHelpersRepository
import dk.rohdef.helperplanning.helpers.HelperService
import dk.rohdef.helperplanning.helpers.HelperServiceImplementation
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.rfbpa.web.*
import dk.rohdef.rfbpa.web.modules.configuration
import dk.rohdef.rfbpa.web.persistance.helpers.TestHelpers
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week29
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week29To31
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week30
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week31
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek29
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek30
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.weekPlanWeek31
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

class ShiftsTest : RfbpaSpec({
    val url = "/api/public/shifts"
    val urlWeek29To31 = "${url}/${week29To31}"

    val weekPlanService = TestWeekPlanService()
    val helperService = HelperServiceImplementation(MemoryHelpersRepository())
    beforeEach {
        weekPlanService.reset()

        startKoin {
            modules(
                configuration(TestConfiguration.default),
                module {
                    single<Clock> { Clock.System }
                    single<JwkProvider> { JwkProvider { jwk } }

                    // TODO: 29/10/2024 rohdef - introduce test helper service
                    // TODO: 29/10/2024 rohdef - consider moving towards end to end
                    singleOf(::MemoryHelpersRepository) bind HelpersRepository::class
                    singleOf(::HelperServiceImplementation) bind HelperService::class

                    single<HelperService> { helperService }

                    single<WeekPlanService> { weekPlanService }
                },
            )
        }
    }

    afterEach {
        stopKoin()
    }

    val fiktivusSubject = PrincipalsTestData.FiktivusMaximus.subject
    context("Reading shifts") {
        val helpers = mapOf(
            TestHelpers.fiktivus.id to TestHelpers.fiktivus,
            TestHelpers.realis.id to TestHelpers.realis,
        )

        restTest("Requesting single week") { client ->
            val response = client.get("$url/$week29--$week29")

            response.status shouldBe HttpStatusCode.OK
            val weekPlans: List<WeekPlanOut> = response.body()
            weekPlans shouldBe listOf(
                WeekPlanOut.from(WeekPlan.emptyPlan(week29), emptyMap()),
            )
        }

        restTest("No shifts gives an empty week plans") { client ->
            val response = client.get(urlWeek29To31)

            response.status shouldBe HttpStatusCode.OK
            val weekPlans: List<WeekPlanOut> = response.body()
            weekPlans shouldBe listOf(
                WeekPlanOut.from(WeekPlan.emptyPlan(week29), emptyMap()),
                WeekPlanOut.from(WeekPlan.emptyPlan(week30), emptyMap()),
                WeekPlanOut.from(WeekPlan.emptyPlan(week31), emptyMap()),
            )
        }

        restTest("Querying multiple shifts") { client ->
            // TODO: 29/10/2024 rohdef - helpers definitely needs some rework
            helperService.create(TestHelpers.fiktivus)
            helperService.create(TestHelpers.realis)

            // TODO add items to system - maybe lift to all tests
            // query multiple weeks
            weekPlanWeek29.allShifts.forEach { weekPlanService.addShift(fiktivusSubject, it) }
            weekPlanWeek30.allShifts.forEach { weekPlanService.addShift(fiktivusSubject, it) }
            weekPlanWeek31.allShifts.forEach { weekPlanService.addShift(fiktivusSubject, it) }

            val response = client.get(urlWeek29To31)

            response.status shouldBe HttpStatusCode.OK
            val weekPlans: List<WeekPlanOut> = response.body()
            weekPlans shouldBe listOf(
                WeekPlanOut.from(weekPlanWeek29, helpers),
                WeekPlanOut.from(weekPlanWeek30, helpers),
                WeekPlanOut.from(weekPlanWeek31, helpers),
            )
        }

        xrestTest("Helper bookings") {}
    }

    xcontext("Reporting illness") {
        restTest("for existing shift") { client ->
            val response = client.put("$url/id/registrations/illness")

            response.status shouldBe HttpStatusCode.OK

            val newShiftId: UUID = response.body()
            // TODO can it (should it?) be fixed
        }

        // TODO what are potential errors
    }

    xrestTest("Authentication from salary system error should be communicated to the client") { client ->
        val response = client.get(url)

        response.status shouldBe HttpStatusCode.Unauthorized
        // TODO: 26/07/2024 rohdef - how do we model a decent error?
        val error: String = response.body()
    }

    xrestTest("Sync and shift error should be communicated to the client as server failure") { client ->
        // TODO: 26/07/2024 rohdef - set the status for error

        val response = client.get(url)

        response.status shouldBe HttpStatusCode.InternalServerError
        // TODO: 26/07/2024 rohdef - how do we model a decent error?
        val error: String = response.body()
    }

    restTest("Year week parameter is malformed") { client ->
        val response = client.get("$url/week4--week5")
        response.status shouldBe HttpStatusCode.BadRequest

        // TODO: 29/07/2024 rohdef - add proper error, references #21
    }

    restTest("Interval separator is missing") { client ->
        val response = client.get("$url/in-interval/2024-W122024-W15")

        response.status shouldBe HttpStatusCode.BadRequest

        // TODO: 29/07/2024 rohdef - add proper error, references #21
        val error: ErrorDto = response.body()

        error.message.shouldNotBeEmpty()
        error.supplementary shouldBe NoData
        error.type shouldBe UnknownError
    }
})
