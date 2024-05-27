package dk.rohdef.rfbpa.commands

import arrow.core.Either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.enum
import dk.rohdef.helperplanning.shifts.ShiftType
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.io.Closeable

class CreateShift(
    // TODO: 23/04/2024 rohdef - don't use repository directly
    private val weekPlansRepository: WeekPlanRepository,
): CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val type: ShiftType by argument()
        .enum()

    private val start: Instant by argument()
        .convert {
            // // TODO: 28/04/2024 rohdef - these should be kept in local date time it seems
            LocalDateTime.parse(it).toInstant(TimeZone.of("Europe/Copenhagen"))
        }
        .help("Start date of shift in ISO w.o. timezone (assumes Europe/Copenhagen)")
    private val end: Instant by argument()
        .convert {
            // // TODO: 28/04/2024 rohdef - these should be kept in local date time it seems
            LocalDateTime.parse(it).toInstant(TimeZone.of("Europe/Copenhagen"))
        }
        .help("End date of shift in ISO w.o. timezone (assumes Europe/Copenhagen)")

    override fun run() = runBlocking {
        log.info { "Creating a new shift" }
//        val hmm = Instant.parse("2024-W18-2", kotlinx.datetime.format.DateTimeFormat.formatAsKotlinBuilderDsl())
//        println(hmm)
//        TODO()
        val bookingId = weekPlansRepository.createShift(start, end, type)

        when (bookingId) {
            is Either.Right -> {
                log.info { "Successfully booked" }
                log.info { "$start -- $end" }
                log.info { "${bookingId.value}" }
            }
            is Either.Left -> {
                log.error { "Could not book ${bookingId.value} shift: $start -- $end" }
            }
        }

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }
}
