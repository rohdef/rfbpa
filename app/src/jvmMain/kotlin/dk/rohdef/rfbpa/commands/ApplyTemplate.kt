package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.helperplanning.templates.Template
import dk.rohdef.helperplanning.templates.TemplateApplier
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.mamoe.yamlkt.Yaml
import java.io.Closeable
import java.io.File

class ApplyTemplate(
    private val templateApplier: TemplateApplier,
) : CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val yearWeekInterval by argument()
        .toYearWeekInterval()

    private val templateFile by argument()
        .convert { File(it) }
        .help { "File with yaml formatted shift template" }

    override fun run(): Unit = runBlocking {
        log.info { "Applying template" }
        val rawTemplate = templateFile.readText()
        val template = Yaml.decodeFromString(Template.serializer(), rawTemplate)

        templateApplier.applyTemplates(yearWeekInterval, listOf(template))

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }
}
