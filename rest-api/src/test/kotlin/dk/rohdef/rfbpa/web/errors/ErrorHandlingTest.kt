package dk.rohdef.rfbpa.web.errors

import com.auth0.jwk.JwkProviderBuilder
import dk.rohdef.rfbpa.web.RfbpaSpec
import dk.rohdef.rfbpa.web.TestConfiguration
import dk.rohdef.rfbpa.web.TestWeekPlanService
import dk.rohdef.rfbpa.web.health.HealthDto
import dk.rohdef.rfbpa.web.health.HealthService
import dk.rohdef.rfbpa.web.modules.configuration
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

class ErrorHandlingTest : RfbpaSpec({
    val url = "/health"

    val weekPlanService = TestWeekPlanService()
    beforeEach {
        weekPlanService.reset()

        startKoin {
            modules(
                configuration(TestConfiguration.default),
                module { single {
                    JwkProviderBuilder("localhost").build()
                } },
                module {
                    singleOf(::FailingHealthService) bind HealthService::class
                },
            )
        }
    }

    afterEach {
        stopKoin()
    }

    restTest("Interval separator is missing") { client ->
        val response = client.get(url)

        response.status shouldBe HttpStatusCode.BadRequest

        // TODO: 29/07/2024 rohdef - add proper error, references #21
        val error: ErrorDto = response.body()

        error.message.shouldNotBeEmpty()
        error.supplementary shouldBe NoData
        error.type shouldBe UnknownError
    }
})

class FailingHealthService : HealthService {
    override fun healthStatus(): HealthDto {
        throw Throwable("Bad error, should be captured")
    }
}
