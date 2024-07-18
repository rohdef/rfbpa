package dk.rohdef.rfbpa.web.plugins

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.AxpSalarySystem
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.rfbpa.web.DatabaseAxpToDomainmapper
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.MemorySalarySystemRepository
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.HelperDataBaseItem
import dk.rohdef.rfbpa.web.LoggingSalarySystemRepository
import dk.rohdef.rfbpa.web.MemoryAxpRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.nio.file.Paths
import kotlin.io.path.readText

fun Application.dependencyInjection() {
    val log = KotlinLogging.logger {}

    val configurationRaw = environment.config.toMap()["rfbpa"]!! as Map<String, Any>
    val configuration = RfBpaConfig.fromMap(configurationRaw)

    when (configuration.runtimeMode) {
        RuntimeMode.DEVELOPMENT -> log.warn { "Running in development mode" }
        RuntimeMode.PRODUCTION -> log.info { "Running in production mode" }
    }

    install(Koin) {
        // configuration
        val config = module {
            single<RfBpaConfig> { configuration }

            single<Map<String, HelperDataBaseItem>>(named("helpers")) {
                val helpers = Paths.get("helpers.yaml")
                    .readText()
                Yaml.decodeFromString<Map<String, HelperDataBaseItem>>(helpers)
            }

            single<AxpToDomainMapper> {
                DatabaseAxpToDomainmapper(Clock.System)
            }

            single<AxpRepository> {
                val helpers = get<Map<String, HelperDataBaseItem>>(named("helpers"))
                    .map { it.value }
                MemoryAxpRepository(helpers)
            }

            single<AxpConfiguration> {
                val config: RfBpaConfig = get()

                AxpConfiguration(
                    TimeZone.of("Europe/Copenhagen"),
                    config.axp.host,
                    config.axp.username,
                    config.axp.password,
                )
            }
        }

        // web module
        val web = module {
            when (configuration.runtimeMode) {
                RuntimeMode.DEVELOPMENT -> single<SalarySystemRepository> {
                    LoggingSalarySystemRepository(
                        MemorySalarySystemRepository()
                    )
                }

                RuntimeMode.PRODUCTION -> singleOf(::AxpSalarySystem) bind SalarySystemRepository::class
            }
        }

        modules(
            config,
            web,
        )
    }
}
