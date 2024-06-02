package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.rfbpa.ShiftsReader
import kotlinx.coroutines.runBlocking
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable

class UnbookedShifts(
    private val shiftsReader: ShiftsReader,
) : CliktCommand() {
    private val log = KotlinLogging.logger { }

    private val yearWeekInterval by argument()
        .toYearWeekInterval()

    override fun run(): Unit = runBlocking {
        log.info { "Reading unbooked shifts" }

        shiftsReader.unbookedShifts(yearWeekInterval)

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }
}
