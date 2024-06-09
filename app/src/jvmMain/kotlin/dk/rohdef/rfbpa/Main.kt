package dk.rohdef.rfbpa

import dk.rohdef.axpclient.AxpWeekPlans
import dk.rohdef.axpclient.configuration.AxpConfiguration
import dk.rohdef.helperplanning.MemoryWeekPlanRepository
import dk.rohdef.helperplanning.WeekPlanRepository
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.rfbpa.commands.RfBpa
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.TimeZone
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

    val configurationModule = module {
        val configuration = object {}::class.java
            .getResource("/rfbpa.yaml")!!
            .readText()
        val helpers = object {}::class.java
            .getResource("/helpers.yaml")!!
            .readText()

        single<RfBpaConfig> { Yaml.decodeFromString(RfBpaConfig.serializer(), configuration) }
        single<Map<String, String>>(named("helpers")) {
            Yaml.decodeMapFromString(helpers)
                .mapKeys { it.key!! }
                .mapValues { it.value.toString() }
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
        single<WeekPlanRepository> { LoggingWeekPlanRepository(MemoryWeekPlanRepository()) }
//        singleOf(::AxpWeekPlans) bind WeekPlanRepository::class
        single { TemplateApplier(get(), get(named("helpers"))) }
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
