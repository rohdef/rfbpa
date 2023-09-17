package dk.rohdef.rfbpa.commands

import arrow.core.Either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.enum
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.ShiftType
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import mu.KotlinLogging
import java.io.Closeable

class BookShift(
    // TODO don't use repo directly
    private val weekPlanRepository: WeekPlanRepository,
    private val helpers: Map<String, String>,
): CliktCommand() {
    private val log = KotlinLogging.logger { }

    private val type: ShiftType by argument()
        .enum()

    private val helper: HelperBooking by argument()
        .convert { HelperBooking.PermanentHelper(helpers[it]!!) }
        .help("ID of the helper to request data for")

    private val start: Instant by argument()
        .convert {
            LocalDateTime.parse(it).toInstant(TimeZone.of("Europe/Copenhagen"))
        }
        .help("Start date of shift in ISO w.o. timezone (assumes Europe/Copenhagen)")
    private val end: Instant by argument()
        .convert {
            LocalDateTime.parse(it).toInstant(TimeZone.of("Europe/Copenhagen"))
        }
        .help("End date of shift in ISO w.o. timezone (assumes Europe/Copenhagen)")

    override fun run(): Unit = runBlocking {
        log.info { "Booking shift" }

        val bookingId = weekPlanRepository.bookShift(
            helper,
            type,
            start,
            end,
        )

        when (bookingId) {
            is Either.Right -> {
                log.info { "Successfully booked" }
                log.info { "Helper: $helper" }
                log.info { "$start -- $end" }
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