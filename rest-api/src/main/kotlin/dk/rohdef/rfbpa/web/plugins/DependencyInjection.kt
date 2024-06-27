package dk.rohdef.rfbpa.web.plugins

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.AxpWeekPlans
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.MemoryWeekPlanRepository
import dk.rohdef.helperplanning.WeekPlanRepository
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.HelperDataBaseItem
import dk.rohdef.rfbpa.web.LoggingWeekPlanRepository
import dk.rohdef.rfbpa.web.MemoryAxpRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlinx.datetime.TimeZone
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Application.dependencyInjection() {
    val log = KotlinLogging.logger {}

//    val helpers = object {}::class.java
//        .getResource("/helpers.yaml")!!
//        .readText()

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
//                Yaml.decodeFromString<Map<String, HelperDataBaseItem>>(helpers)
                emptyMap()
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
                RuntimeMode.DEVELOPMENT ->  single<WeekPlanRepository> { LoggingWeekPlanRepository(MemoryWeekPlanRepository()) }
                RuntimeMode.PRODUCTION -> singleOf(::AxpWeekPlans) bind WeekPlanRepository::class
            }
        }

        modules(
            config,
            web,
        )
    }
}
