package dk.rohdef.rfbpa.commands

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.rfbpa.templates.HelperReservation
import dk.rohdef.rfbpa.templates.Template
import dk.rohdef.rfbpa.templates.WeekTemplate
import dk.rohdef.rfweeks.YearWeek
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import net.mamoe.yamlkt.Yaml
import java.io.Closeable
import java.io.File

class ApplyTemplate(
    // TODO: 23/04/2024 rohdef - don't use repository directly
    private val weekPlanRepository: WeekPlanRepository,
    private val helpers: Map<String, String>,
) : CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val templateFile by argument()
        .convert { File(it) }
        .help { "File with yaml formatted shift template" }

    override fun run(): Unit = runBlocking {
        val rawTemplate = templateFile.readText()

        val template = Yaml.decodeFromString(Template.serializer(), rawTemplate)

        // TODO: 02/06/2024 rohdef - this part is quite testable
        val weekStart = YearWeek(2024, 25)
        val weekEnd = YearWeek(2024, 33) // three rolls in new


        val templateStart = maxOf(template.start, weekStart)
        val templateEnd = template.end
            .map { minOf(it, weekEnd) }
            .getOrElse { weekEnd }
        log.info { "Applying template in interval ${templateStart}--${templateEnd}" }

        (templateStart..templateEnd).forEach {
            applyWeekTemplates(it, template.weeks)
        }

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }

    suspend fun applyWeekTemplates(week: YearWeek, weekTemplates: List<WeekTemplate>) {
        weekTemplates.forEach { weekTemplate ->
            log.info { weekTemplate.name }
            val shifts = weekTemplate.shifts
            shifts.forEach {
                val yearWeekDay = week.atDayOfWeek(it.key)
                val localDate = yearWeekDay.toLocalDate()

                it.value.forEach {
                    val start = localDate.atTime(it.start)
                    val end = start.untilTime(it.end)

                    // TODO: 02/06/2024 rohdef - deal with time zones somehow, not fond of hard code
                    val startInstant = start.toInstant(TimeZone.of("Europe/Copenhagen"))
                    val endInstant = end.toInstant(TimeZone.of("Europe/Copenhagen"))

                    // TODO: 02/06/2024 rohdef - re-introduce once better test
//                    val shiftId = weekPlanRepository.createShift(startInstant, endInstant, it.type)
                    val shiftId = ShiftId("Dummy").right()
                    log.info { "\tcreated shift: ${start}--${end}" }

                    when (shiftId) {
                        is Either.Right -> {
                            log.info { "\t\t${shiftId.value}" }

                            bookHelper(shiftId.value, it.helper)
                        }
                        is Either.Left -> {
                            log.error { "Could not book ${shiftId.value} shift: $start -- $end" }
                        }
                    }
                }
            }
        }
    }

    private suspend fun bookHelper(shiftId: ShiftId, helper: HelperReservation) {
        when (helper) {
            is HelperReservation.Helper -> {
                val helper = HelperBooking.PermanentHelper(helpers[helper.id]!!)

                // TODO: 02/06/2024 rohdef - re-introduce after manual tests - perhaps some automation?
//                val bookingId = weekPlanRepository.bookShift(
//                    shiftId,
//                    helper,
//                )
//
//                when (bookingId) {
//                    is Either.Right -> log.info { "Successfully booked ${helper}" }
//                    is Either.Left -> log.error { "Could not book shift" }
//                }
            }
            HelperReservation.NoReservation -> log.info { "No helper specified" }
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
