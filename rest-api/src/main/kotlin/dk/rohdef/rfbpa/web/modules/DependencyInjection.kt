package dk.rohdef.rfbpa.web.modules

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.AxpSalarySystem
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.*
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceImplementation
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.HelperDataBaseItem
import dk.rohdef.rfbpa.web.LoggingSalarySystemRepository
import dk.rohdef.rfbpa.web.MemoryAxpRepository
import dk.rohdef.rfbpa.web.persistance.axp.DatabaseAxpToDomainmapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
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
    singleOf(::DatabaseAxpToDomainmapper) bind AxpToDomainMapper::class
    singleOf(::MemoryShiftRepository) bind ShiftRepository::class
    singleOf(::MemoryWeekSynchronizationRepository) bind WeekSynchronizationRepository::class
    single<AxpRepository> {
        val helpers = Paths.get("helpers.yaml").readText()
            .let { Yaml.decodeFromString<Map<String, HelperDataBaseItem>>(it) }
            .map { it.value }
        MemoryAxpRepository(helpers)
    }

    val loggingMemoryRepository = LoggingSalarySystemRepository(MemorySalarySystemRepository())
    when (rfBpaConfig.runtimeMode) {
        RuntimeMode.DEVELOPMENT -> single<SalarySystemRepository> { loggingMemoryRepository }
        RuntimeMode.TEST -> single<SalarySystemRepository> { loggingMemoryRepository }
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
            configuration(rfBpaConfig),
            repositories(rfBpaConfig),
            module { singleOf(::WeekPlanServiceImplementation) bind WeekPlanService::class },
        )
    }
}
