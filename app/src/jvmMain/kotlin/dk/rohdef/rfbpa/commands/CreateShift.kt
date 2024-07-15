package dk.rohdef.rfbpa.commands

import arrow.core.Either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.enum
import dk.rohdef.helperplanning.shifts.ShiftType
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import java.io.Closeable

class CreateShift(
    // TODO: 23/04/2024 rohdef - don't use repository directly
    private val salarySystemRepository: SalarySystemRepository,
): CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val type: ShiftType by argument()
        .enum()

    private val start: YearWeekDayAtTime by argument()
        .convert {
            // TODO: 28/04/2024 rohdef - these should use YearWeekDayAtTime once parsing is added
            val x = LocalDateTime.parse(it)
            YearWeekDayAtTime(YearWeekDay.from(x.date), x.time)
        }
        .help("Start date of shift in ISO w.o. timezone")
    private val end: YearWeekDayAtTime by argument()
        .convert {
            // TODO: 28/04/2024 rohdef - these should use YearWeekDayAtTime once parsing is added
            val x = LocalDateTime.parse(it)
            YearWeekDayAtTime(YearWeekDay.from(x.date), x.time)
        }
        .help("End date of shift in ISO w.o. timezone")

    override fun run() = runBlocking {
        log.info { "Creating a new shift" }
        val bookingId = salarySystemRepository.createShift(start, end, type)

        when (bookingId) {
            is Either.Right -> {
                log.info { "Successfully created" }
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
