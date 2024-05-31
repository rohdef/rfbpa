package dk.rohdef.rfbpa.commands

import arrow.core.Either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.io.Closeable

class BookShift(
    // TODO don't use repo directly
    private val weekPlanRepository: WeekPlanRepository,
    private val helpers: Map<String, String>,
): CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val shiftId by argument()
        .convert { ShiftId(it) }
        .help { "ID of the shift" }

    private val helper by argument()
        .convert { HelperBooking.PermanentHelper(helpers[it]!!) }
        .help("ID of the helper to request data for")

    override fun run(): Unit = runBlocking {
        log.info { "Booking shift" }

        val bookingId = weekPlanRepository.bookShift(
            shiftId,
            helper,
        )

        when (bookingId) {
            is Either.Right -> {
                log.info { "Successfully booked" }
                log.info { "Helper: $helper" }
                log.info { "${bookingId.value}" }
            }
            is Either.Left -> {
                log.error { "Could not book shift" }
            }
        }

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }
}
