package dk.rohdef.rfbpa

import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ShiftsReader(private val weekPlansRepository: SalarySystemRepository) {
    private val log = KotlinLogging.logger { }

    suspend fun unbookedShifts(interval: YearWeekInterval) = either {
        log.info { "Reading shifts" }

        val weekPlans = weekPlansRepository
            .shifts(interval)
            .bind()

        printShifts(weekPlans.nonBookedShiftsByDate)
    }

    private val dateFormat = DateTimeFormatter.ofPattern("EEEE 'den' d. MMM:", Locale("da"))
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    fun printShifts(shifts: Map<LocalDate, List<Shift>>) {
        log.info { "Presenting non booked shifts" }

        shifts.forEach { date, shifts ->
            println(dateFormat.format(date.toJavaLocalDate()))

            shifts.forEach {
                val start = timeFormat.format(it.start.toJavaLocalDateTime())
                val end = timeFormat.format(it.end.toJavaLocalDateTime())
                println("${start} - ${end}")
            }
            println()
        }
    }
}
