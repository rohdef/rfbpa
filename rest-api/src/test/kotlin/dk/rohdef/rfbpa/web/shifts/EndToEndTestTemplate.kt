package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.AxpShiftReferences
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.WeekSynchronizationRepository
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceImplementation
import dk.rohdef.rfbpa.web.*
import dk.rohdef.rfbpa.web.modules.configuration
import dk.rohdef.rfbpa.web.persistance.axp.DatabaseAxpShiftReferences
import dk.rohdef.rfbpa.web.persistance.axp.HelperDataBaseItem
import dk.rohdef.rfbpa.web.persistance.axp.MemoryAxpHelperReferences
import dk.rohdef.rfweeks.YearWeekDayAtTime
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

class EndToEndTestTemplate : FunSpec({
    // TODO: 26/07/2024 rohdef - change to call actual shifts endpoint
    val url = "/qq"

    // TODO all is disabled right now
    fun FunSpec.restTest(name: String, block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) {
        xtest(name) {
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

    fun FunSpec.xrestTest(name: String, block: suspend ApplicationTestBuilder.() -> Unit) {
        xtest(name) {}
    }

    // TODO: 31/07/2024 rohdef - do we want the actual repository or is it better to simulate errors?
    val shiftRepository = TestShiftRespository()
    val salarySystem = TestSalarySystemRepository()
    val synchronization = TestWeekSynchronizationRepository()

    beforeEach {
        shiftRepository.reset()
        salarySystem.reset()
        synchronization.reset()

        startKoin {
            val repositories = module {
                singleOf(::DatabaseAxpShiftReferences) bind AxpShiftReferences::class
                single<ShiftRepository> { shiftRepository }
                single<WeekSynchronizationRepository> { synchronization }
                single<AxpHelperReferences> {
                    val helpers = listOf(HelperDataBaseItem("x", "y", UUID.generateUUID()))
                    MemoryAxpHelperReferences(helpers)
                }
                single<SalarySystemRepository> { salarySystem }
            }

            modules(
                module { single<Clock> { Clock.System } },
                configuration(TestConfiguration.default),
                repositories,
                module { singleOf(::WeekPlanServiceImplementation) bind WeekPlanService::class },
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
        val weekPlans: List<WeekPlanOut> = response.body()
        weekPlans shouldBe listOf()
    }
})
