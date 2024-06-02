package dk.rohdef.rfbpa.commands

import arrow.core.getOrElse
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.rfbpa.templates.Template
import dk.rohdef.rfbpa.templates.WeekTemplate
import dk.rohdef.rfweeks.YearWeek
import io.github.oshai.kotlinlogging.KotlinLogging
import net.mamoe.yamlkt.Yaml
import java.io.File

class ApplyTemplate(
) : CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val templateFile by argument()
        .convert { File(it) }
        .help { "File with yaml formatted shift template" }

    override fun run() {
        val rawTemplate = templateFile.readText()

        val template = Yaml.decodeFromString(Template.serializer(), rawTemplate)

        // TODO: 02/06/2024 rohdef - this part is quite testable
        val weekStart = YearWeek(2024, 24)
        val weekEnd = YearWeek(2024, 33) // three rolls in new

        log.info { "Applying template" }

        val templateStart = maxOf(template.start, weekStart)
        val templateEnd = template.end
            .map { minOf(it, weekEnd) }
            .getOrElse { weekEnd }

        (templateStart..templateEnd).forEach {
            applyWeekTemplates(it, template.weeks)
        }
    }

    fun applyWeekTemplates(week: YearWeek, weekTemplates: List<WeekTemplate>) {
        weekTemplates.forEach { weekTemplate ->
            log.info { weekTemplate.name }
            // TODO: 02/06/2024 rohdef - use map with default value instead
            val shifts = weekTemplate.shifts
            shifts.forEach {
                val yearWeekDay = week.atDayOfWeek(it.key)
                log.info {
                    "${yearWeekDay.week} ${yearWeekDay.dayOfWeek}:"
                }
                it.value.forEach {
                    log.info { "\t${it.helper} - ${it.start}--${it.end}" }
                }
            }
        }
    }
}
