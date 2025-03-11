@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.modules

import com.auth0.jwk.JwkProviderBuilder
import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.AxpSalarySystem
import dk.rohdef.axpclient.AxpShiftReferences
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.helpers.HelperService
import dk.rohdef.helperplanning.helpers.HelperServiceImplementation
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceImplementation
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.LoggingSalarySystemRepository
import dk.rohdef.rfbpa.web.Seeder
import dk.rohdef.rfbpa.web.health.HealthService
import dk.rohdef.rfbpa.web.health.HealthServiceImplementation
import dk.rohdef.rfbpa.web.persistance.axp.DatabaseAxpHelperReferences
import dk.rohdef.rfbpa.web.persistance.axp.DatabaseAxpShiftReferences
import dk.rohdef.rfbpa.web.persistance.helpers.DatabaseHelpers
import dk.rohdef.rfbpa.web.persistance.shifts.DatabaseShifts
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.uuid.ExperimentalUuidApi

fun KoinApplication.configuration(rfBpaConfig: RfBpaConfig): Module = module {
    single<RfBpaConfig> { rfBpaConfig }

    single<AxpConfiguration> {
        rfBpaConfig.axp.let { AxpConfiguration(it.host, it.username, it.password) }
    }
}

fun KoinApplication.repositories(rfBpaConfig: RfBpaConfig): Module = module {
    singleOf(::DatabaseAxpShiftReferences) bind AxpShiftReferences::class
    singleOf(::DatabaseShifts) bind ShiftRepository::class
    singleOf(::MemoryWeekSynchronizationRepository) bind WeekSynchronizationRepository::class

    singleOf(::DatabaseHelpers) bind HelpersRepository::class
    singleOf(::DatabaseAxpHelperReferences) bind AxpHelperReferences::class

    singleOf(::Seeder) bind Seeder::class

    when (rfBpaConfig.runtimeMode) {
        RuntimeMode.DEVELOPMENT -> single<SalarySystemRepository> { LoggingSalarySystemRepository(MemorySalarySystemRepository()) }
        RuntimeMode.TEST -> single<SalarySystemRepository> { LoggingSalarySystemRepository(MemorySalarySystemRepository()) }
        RuntimeMode.PRODUCTION -> singleOf(::AxpSalarySystem) bind SalarySystemRepository::class
    }
}

fun Application.dependencyInjection() {
    val log = KotlinLogging.logger {}

    val configurationRaw = environment.config.toMap()["rfbpa"]!! as Map<String, Any>
    val rfBpaConfig = RfBpaConfig.fromMap(configurationRaw)

    when (rfBpaConfig.runtimeMode) {
        RuntimeMode.DEVELOPMENT -> log.warn { "Running in development mode" }
        RuntimeMode.PRODUCTION -> log.info { "Running in production mode" }
        RuntimeMode.TEST -> log.info { "Running in test mode" }
    }

    install(Koin) {
        modules(
            module { singleOf(::HealthServiceImplementation) bind HealthService::class },

            module { single<Clock> { Clock.System } },
            module { single {
                JwkProviderBuilder(rfBpaConfig.auth.jwkEndpoint).build()
            } },
            configuration(rfBpaConfig),
            repositories(rfBpaConfig),
            module { singleOf(::HelperServiceImplementation) bind HelperService::class },
            module { singleOf(::WeekPlanServiceImplementation) bind WeekPlanService::class },
            module { singleOf(::TemplateApplier) bind TemplateApplier::class },
        )
    }
}
