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
import kotlinx.datetime.*
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


        val templateStart = maxOf(template.start, weekStart)
        val templateEnd = template.end
            .map { minOf(it, weekEnd) }
            .getOrElse { weekEnd }
        log.info { "Applying template in interval ${templateStart}--${templateEnd}" }

        (templateStart..templateEnd).forEach {
            applyWeekTemplates(it, template.weeks)
        }
    }

    fun applyWeekTemplates(week: YearWeek, weekTemplates: List<WeekTemplate>) {
        weekTemplates.forEach { weekTemplate ->
            log.info { weekTemplate.name }
            val shifts = weekTemplate.shifts
            shifts.forEach {
                val yearWeekDay = week.atDayOfWeek(it.key)
                val localDate = yearWeekDay.toLocalDate()

                log.info {
                    "${yearWeekDay.week} ${yearWeekDay.dayOfWeek}:"
                }
                it.value.forEach {
                    val start = localDate.atTime(it.start)
                    val end = start.untilTime(it.end)
                    log.info { "\t${it.helper} - ${start}--${end}" }
                }
            }
        }
    }

    private fun LocalDateTime.untilTime(time: LocalTime): LocalDateTime {
        val correctedDate = if (time < this.time) {
            this.date.plus(1, DateTimeUnit.DAY)
        } else {
            this.date
        }

        return correctedDate.atTime(time)
    }
}
