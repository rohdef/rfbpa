package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.rfbpa.ShiftsReader
import kotlinx.coroutines.runBlocking
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable

class HoursWorked(
    private val shiftsReader: ShiftsReader,
    private val helpers: Map<String, String>,
) : CliktCommand() {
    private val log = KotlinLogging.logger { }

    private val yearWeekRange by argument()
        .toYearWeekRange()
        .help(durationFormat)
    private val helper: HelperBooking by argument()
        .convert { HelperBooking.PermanentHelper(helpers[it]!!) }
        .help("ID of the helper to request data for")

    override fun run(): Unit = runBlocking {
        log.info { "Calculating hours for helper" }

        shiftsReader.hoursWorked(yearWeekRange, helper as HelperBooking.PermanentHelper)

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }
}
