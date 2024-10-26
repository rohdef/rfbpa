package dk.rohdef.rfbpa.web.modules

import com.auth0.jwk.JwkProviderBuilder
import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.AxpSalarySystem
import dk.rohdef.axpclient.AxpShiftReferences
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceImplementation
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.LoggingSalarySystemRepository
import dk.rohdef.rfbpa.web.persistance.axp.DatabaseAxpShiftReferences
import dk.rohdef.rfbpa.web.persistance.axp.HelperDataBaseItem
import dk.rohdef.rfbpa.web.persistance.axp.MemoryAxpHelperReferences
import dk.rohdef.rfbpa.web.persistance.helpers.DatabaseHelpers
import dk.rohdef.rfbpa.web.persistance.shifts.DatabaseShifts
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.nio.file.Paths
import kotlin.io.path.readText

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

    // TODO: 01/08/2024 rohdef - find better way to inject test data
    val helpers = Paths.get("helpers.yaml").readText()
        .let { Yaml.decodeFromString<Map<String, HelperDataBaseItem>>(it) }
    single<HelpersRepository> {
        DatabaseHelpers().apply {
            runBlocking {
                helpers.map {
                    Helper.Permanent(it.key, it.key, HelperId(it.value.id))
                }.forEach {
                    create(it)
                }
            }
        }

    }
//    singleOf(::DatabaseHelpers) bind HelpersRepository::class
    single<AxpHelperReferences> {
        MemoryAxpHelperReferences(helpers.map { it.value })
    }

    when (rfBpaConfig.runtimeMode) {
        RuntimeMode.DEVELOPMENT -> single<SalarySystemRepository> { LoggingSalarySystemRepository(MemorySalarySystemRepository(get())) }
        RuntimeMode.TEST -> single<SalarySystemRepository> { LoggingSalarySystemRepository(MemorySalarySystemRepository(get())) }
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
            module { single<Clock> { Clock.System } },
            module { single {
                JwkProviderBuilder(rfBpaConfig.auth.jwkEndpoint).build()
            } },
            configuration(rfBpaConfig),
            repositories(rfBpaConfig),
            module { singleOf(::WeekPlanServiceImplementation) bind WeekPlanService::class },
            module { singleOf(::TemplateApplier) bind TemplateApplier::class },
        )
    }
}
