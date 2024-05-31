package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.rfbpa.templates.Template
import io.github.oshai.kotlinlogging.KotlinLogging
import net.mamoe.yamlkt.Yaml
import java.io.File

class ApplyTemplate(
): CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val templateFile by argument()
        .convert { File(it) }
        .help { "File with yaml formatted shift template" }

    override fun run() {
        val template = templateFile.readText()

        val q = Yaml.decodeFromString(Template.serializer(), template)
        log.error { q }
    }
}
