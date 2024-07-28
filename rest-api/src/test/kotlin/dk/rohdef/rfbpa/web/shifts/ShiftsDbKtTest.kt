package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.rfbpa.configuration.Axp
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.HelperDataBaseItem
import dk.rohdef.rfbpa.web.MemoryAxpRepository
import dk.rohdef.rfbpa.web.configuration.Auth
import dk.rohdef.rfbpa.web.persistance.axp.DatabaseAxpToDomainmapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.net.URL

class ShiftsDbKtTest : FunSpec({
    // TODO: 26/07/2024 rohdef - change to call actual shifts endpoint
    val url = "/qq"

    fun FunSpec.restTest(name: String, block: suspend ApplicationTestBuilder.(client: HttpClient)->Unit) {
        test(name) {
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
    }

    fun FunSpec.xrestTest(name: String, block: suspend ApplicationTestBuilder.()->Unit) {
        xtest(name) {}
    }

    beforeEach {
        val configuration = RfBpaConfig(
            Axp("", "", ""),
            Auth(
                URL("http://localhost:1234/test"),
                "http://localhost:1234/test"
            ),
            RuntimeMode.TEST,
        )

        startKoin {
            val config = module {
                single<RfBpaConfig> { configuration }

                single<AxpConfiguration> {
                    val config: RfBpaConfig = get()

                    AxpConfiguration(
                        TimeZone.of("Europe/Copenhagen"),
                        config.axp.host,
                        config.axp.username,
                        config.axp.password,
                    )
                }

                single<Clock> { Clock.System }

                singleOf(::DatabaseAxpToDomainmapper) bind AxpToDomainMapper::class

                singleOf(::MemoryShiftRepository) bind ShiftRepository::class
                singleOf(::MemoryWeekSynchronizationRepository) bind WeekSynchronizationRepository::class
                singleOf(::WeekPlanService) bind WeekPlanService::class

                single<AxpRepository> {
                    val helpers = listOf(HelperDataBaseItem("x", "y", UUID.generateUUID()))
                    MemoryAxpRepository(helpers)
                }
                singleOf(::MemorySalarySystemRepository) bind SalarySystemRepository::class
            }

            modules(
                config,
            )
        }
    }

    afterEach {
        stopKoin()
    }

    restTest("No shifts gives an empty list") { client ->
        val response = client.get(url)

        response.status shouldBe HttpStatusCode.OK
        val weekPlans: List<String> = response.body()
        weekPlans.shouldBeEmpty()
    }

    restTest("No shifts gives an empty list") { client ->
        // TODO add items to system - maybe lift to all tests
        // query multiple weeks

        val response = client.get(url)

        response.status shouldBe HttpStatusCode.OK
        val weekPlans: List<String> = response.body()
        weekPlans shouldBe listOf(
            "dummy for now",
        )
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

    xrestTest("Query params missing") {}
    xrestTest("Query params start is missing") {}
    xrestTest("Query params start is malformed") {}
    xrestTest("Query params end is missing") {}
    xrestTest("Query params end is malformed") {}
    xrestTest("Query params end is before start") {}
})
