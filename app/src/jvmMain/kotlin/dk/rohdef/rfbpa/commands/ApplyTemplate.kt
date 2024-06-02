package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.rfbpa.templates.Template
import dk.rohdef.rfweeks.YearWeek
import io.github.oshai.kotlinlogging.KotlinLogging
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.time.DayOfWeek

class ApplyTemplate(
): CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val templateFile by argument()
        .convert { File(it) }
        .help { "File with yaml formatted shift template" }

    override fun run() {
        val rawTemplate = templateFile.readText()

        val template = Yaml.decodeFromString(Template.serializer(), rawTemplate)

        val weekStart = YearWeek(2024, 24)
        val weekEnd = YearWeek(2024, 33) // three rolls in new

        // TODO implement start/end rules
        log.info { "Applying template" }
        template.weeks.forEach {
            log.info { it.name }
            // TODO: 02/06/2024 rohdef - use map with default value instead
            val mondayShifts = it.shifts.getOrDefault(DayOfWeek.MONDAY, emptyList())
            log.info { "Monday: " + mondayShifts }
        }
    }
}
