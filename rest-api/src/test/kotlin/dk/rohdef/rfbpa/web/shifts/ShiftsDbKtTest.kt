package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.rfbpa.web.*
import dk.rohdef.rfbpa.web.modules.configuration
import dk.rohdef.rfbpa.web.persistance.axp.DatabaseAxpToDomainmapper
import dk.rohdef.rfweeks.YearWeekDayAtTime
import generateTestShiftId
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
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

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

    val shiftRepository = TestShiftRespository()
    val salarySystem = TestSalarySystemRepository()
    val synchronization = TestWeekSynchronizationRepository()

    beforeEach {
        shiftRepository.reset()
        salarySystem.reset()
        synchronization.reset()

        startKoin {
            val repositories = module {
                singleOf(::DatabaseAxpToDomainmapper) bind AxpToDomainMapper::class
                single<ShiftRepository> { shiftRepository }
                single<WeekSynchronizationRepository> { synchronization }
                single<AxpRepository> {
                    val helpers = listOf(HelperDataBaseItem("x", "y", UUID.generateUUID()))
                    MemoryAxpRepository(helpers)
                }
                single<SalarySystemRepository> { salarySystem }
            }

            modules(
                module { single<Clock> { Clock.System } },
                configuration(TestConfiguration.default),
                repositories,
                module { singleOf(::WeekPlanService) bind WeekPlanService::class },
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
        val start = YearWeekDayAtTime.parseUnsafe("2024-W29-3T11:30")
        val end = YearWeekDayAtTime.parseUnsafe("2024-W29-3T16:30")
        salarySystem.createShift(
            start,
            end,
        )

        val response = client.get(url)

        response.status shouldBe HttpStatusCode.OK
        val weekPlans: List<Shi> = response.body()
        weekPlans shouldBe listOf(
            Shi(
                generateTestShiftId(start, end).id,
                "2024-W29-3T11:30",
                "2024-W29-3T16:30",
            ),
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
