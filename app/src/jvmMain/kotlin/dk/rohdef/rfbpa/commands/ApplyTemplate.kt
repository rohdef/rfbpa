package dk.rohdef.rfbpa.commands

import arrow.core.Either
import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.helperplanning.templates.HelperReservation
import dk.rohdef.helperplanning.templates.Template
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.helperplanning.templates.WeekTemplate
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
    private val templateApplier: TemplateApplier,
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

        templateApplier.applyTemplate(weekStart, weekEnd, template)

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }

    suspend fun applyWeekTemplates(week: YearWeek, weekTemplates: List<WeekTemplate>) {
        weekTemplates.forEach { weekTemplate ->

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
}
