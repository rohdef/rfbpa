package dk.rohdef.rfbpa

import arrow.core.raise.either
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftData
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.rfweeks.YearWeekRange
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration

class ShiftsReader(private val weekPlansRepository: WeekPlanRepository) {
    private val log = KotlinLogging.logger { }

    suspend fun unbookedShifts(range: YearWeekRange) = either {
        log.info { "Reading shifts" }

        val weekPlans = weekPlansRepository
            .shifts(range)
            .bind()

        printShifts(weekPlans.nonBookedShiftsByDate)
    }

    suspend fun hoursWorked(range: YearWeekRange, helper: HelperBooking.PermanentHelper) = either {
        val weekPlans = weekPlansRepository
            .shifts(range)
            .bind()

        val shiftsFor = weekPlans.shiftsFor(helper)
        val duration = duration(shiftsFor)

        log.info { duration }
    }

    fun duration(shifts: ShiftData): Duration {
        return when (shifts) {
            ShiftData.NoData -> Duration.parse("PT0M")
            is ShiftData.Shifts -> shifts.shifts
                .foldRight(Duration.parse("PT0M")) { shift, acc ->
                    val end = shift.end.toInstant(TimeZone.of("Europe/Copenhagen"))
                    val start = shift.start.toInstant(TimeZone.of("Europe/Copenhagen"))
                    val duration = end - start

                    acc + duration
                }
        }
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
