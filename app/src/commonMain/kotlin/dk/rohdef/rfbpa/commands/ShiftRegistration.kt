package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.enum
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.ShiftRegistration
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Closeable

class ShiftRegistration(
    private val weekPlanRepository: WeekPlanRepository,
): CliktCommand() {
    private val log = KotlinLogging.logger { }

    private val shiftIdtype: ShiftId by argument()
        .convert { ShiftId(it) }
        .help("ID of the booking")

    private val registration: ShiftRegistration by argument()
        .enum()

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

    override fun run() {
        log.info { "Reading unbooked shifts" }

//        weekPlanRepository.registrate()

        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }
}
