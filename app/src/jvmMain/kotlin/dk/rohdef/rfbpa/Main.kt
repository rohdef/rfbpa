package dk.rohdef.rfbpa

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.AxpWeekPlans
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.MemoryWeekPlanRepository
import dk.rohdef.helperplanning.WeekPlanRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.rfbpa.commands.RfBpa
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.TimeZone
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

fun main(cliArguments: Array<String>) {
    val log = KotlinLogging.logger { }
    log.info { "Reading shifts from system - awesomeness upcoming" }

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

    val configurationModule = module {
        single<RfBpaConfig> { configuration }
        single<Map<String, HelperDataBaseItem>>(named("helpers")) {
            Yaml.decodeFromString<Map<String, HelperDataBaseItem>>(helpers)
//                .mapKeys { it.key!! }
//                .mapValues { it.value.toString() }
        }

        single<AxpRepository> {
            val helpers = get<Map<String, HelperDataBaseItem>>(named("helpers"))
                .map { it.value }
            MemoryAxpRepository(helpers)
        }

        single<AxpConfiguration> {
            val config: RfBpaConfig = get()
            val axp = config.client.axp

            AxpConfiguration(
                TimeZone.of("Europe/Copenhagen"),
                axp.url,
                axp.username,
                axp.password,
            )
        }
    }

    val appModule = module {
        when (configuration.runtimeMode) {
            RuntimeMode.DEVELOPMENT ->  single<WeekPlanRepository> { LoggingWeekPlanRepository(MemoryWeekPlanRepository()) }
            RuntimeMode.PRODUCTION -> singleOf(::AxpWeekPlans) bind WeekPlanRepository::class
        }

        single {
            val helpers = get<Map<String, HelperDataBaseItem>>(named("helpers"))
                .mapValues { Helper.ID(it.value.id) }
            TemplateApplier(get(), helpers)
        }
        singleOf(::ShiftsReader)
    }

    startKoin {
        modules(
            configurationModule,
            appModule,
            RfBpa.module,
        )
    }

    RfBpaApplication().main(cliArguments)
}

class RfBpaApplication : KoinComponent {
    val app: RfBpa by inject()

    fun main(cliArguments: Array<String>) = app.main(cliArguments)
}
