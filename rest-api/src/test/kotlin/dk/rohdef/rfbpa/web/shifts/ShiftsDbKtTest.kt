package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.rfbpa.web.TestConfiguration
import dk.rohdef.rfbpa.web.TestWeekPlanService
import dk.rohdef.rfbpa.web.modules.configuration
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.shiftW29Wednesday1
import dk.rohdef.rfbpa.web.persistance.shifts.TestShifts.week29To31
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.kotest.core.test.TestScope
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class ShiftsDbKtTest : FunSpec({
    val url = "/shifts"
    val urlWeek29To31 = "${url}/${week29To31}"

    fun rfbpaTestApplication(
        block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
    ) {
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    json()
                }
            }

            environment {
                developmentMode = false
            }

            block(client)
        }
    }

    fun FunSpecRootScope.restTest(
        name: String,
        block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
    ) = test(name) {
        rfbpaTestApplication(block)
    }

    suspend fun FunSpecContainerScope.restTest(
        name: String,
        block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit,
    ) = test(name) {
        rfbpaTestApplication(block)
    }

    fun FunSpec.xrestTest(name: String, block: suspend ApplicationTestBuilder.() -> Unit) {
        xtest(name) {}
    }

    val weekPlanService = TestWeekPlanService()
    beforeEach {
        weekPlanService.reset()

        startKoin {
            modules(
                module { single<Clock> { Clock.System } },
                configuration(TestConfiguration.default),
                module { single<WeekPlanService> { weekPlanService } },
            )
        }
    }

    afterEach {
        stopKoin()
    }

    context("Reading shifts") {
        restTest("No shifts gives an empty list") { client ->
            val response = client.get(urlWeek29To31)

            response.status shouldBe HttpStatusCode.OK
            val weekPlans: List<String> = response.body()
            weekPlans.shouldBeEmpty()
        }

        restTest("Querying multiple shifts") { client ->
            // TODO add items to system - maybe lift to all tests
            // query multiple weeks
            weekPlanService.addShift(shiftW29Wednesday1)

            val response = client.get(urlWeek29To31)

            response.status shouldBe HttpStatusCode.OK
            val weekPlans: List<Shi> = response.body()
            weekPlans shouldBe listOf(
                Shi(
                    shiftW29Wednesday1.shiftId.id,
                    shiftW29Wednesday1.start.toString(),
                    shiftW29Wednesday1.end.toString(),
                ),
            )
        }
    }

    xrestTest("Authentication from salary system error should be communicated to the client") {
        val response = client.get(url)

        response.status shouldBe HttpStatusCode.Unauthorized
        // TODO: 26/07/2024 rohdef - how do we model a decent error?
        val error: String = response.body()
    }

    xrestTest("Sync and shift error should be communicated to the client as server failure") {
        // TODO: 26/07/2024 rohdef - set the status for error

        val response = client.get(url)

        response.status shouldBe HttpStatusCode.InternalServerError
        // TODO: 26/07/2024 rohdef - how do we model a decent error?
        val error: String = response.body()
    }

    restTest("Year week parameter is malformed") {
        val response = client.get("$url/week4--week5")

        response.status shouldBe HttpStatusCode.BadRequest

        // TODO: 29/07/2024 rohdef - add proper error, references #21
    }

    restTest("Interval separator is missing") {
        val response = client.get("$url/2024-W122024-W15")

        response.status shouldBe HttpStatusCode.BadRequest

        // TODO: 29/07/2024 rohdef - add proper error, references #21
    }
})
