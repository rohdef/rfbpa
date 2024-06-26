package dk.rohdef.rfbpa.web.plugins

import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import net.mamoe.yamlkt.Yaml
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.dependencyInjection() {
    val log = KotlinLogging.logger {}

    val configurationFile = object {}::class.java
        .getResource("/rfbpa.yaml")!!
        .readText()
    val helpers = object {}::class.java
        .getResource("/helpers.yaml")!!
        .readText()

    val configuration = Yaml.decodeFromString(RfBpaConfig.serializer(), configurationFile)

    when(configuration.runtimeMode) {
        RuntimeMode.DEVELOPMENT -> log.warn { "Running in development mode" }
        RuntimeMode.PRODUCTION -> log.info { "Running in production mode" }
    }

    install(Koin) {
        // configuration
        module {
            single<RfBpaConfig> { configuration }
//
//            single<Map<String, HelperDataBaseItem>>(named("helpers")) {
//                Yaml.decodeFromString<Map<String, HelperDataBaseItem>>(helpers)
//            }
//
//            single<AxpRepository> {
//                val helpers = get<Map<String, HelperDataBaseItem>>(named("helpers"))
//                    .map { it.value }
//                MemoryAxpRepository(helpers)
//            }
//
//            single<AxpConfiguration> {
//                val config: RfBpaConfig = get()
//                val axp = config.client.axp
//
//                AxpConfiguration(
//                    TimeZone.of("Europe/Copenhagen"),
//                    axp.url,
//                    axp.username,
//                    axp.password,
//                )
//            }
        }
//
//        // web module
//        module {
//            when (configuration.runtimeMode) {
//                RuntimeMode.DEVELOPMENT ->  single<WeekPlanRepository> { LoggingWeekPlanRepository(MemoryWeekPlanRepository()) }
//                RuntimeMode.PRODUCTION -> singleOf(::AxpWeekPlans) bind WeekPlanRepository::class
//            }
//        }
    }
}
