package dk.rohdef.axpclient.parsing

import arrow.core.Either
import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.axpclient.helper.AxpShift
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.helperplanning.shifts.Weekday
import dk.rohdef.rfweeks.YearWeekDayAtTime
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal class WeekPlanParser(
    val helperRepository: AxpRepository,
    val axpToDomainMapper: AxpToDomainMapper,
) {
    // TODO: 18/07/2024 rohdef - this translates into domain model too early, this should be kept intermediate
    // TODO: 18/07/2024 rohdef - this should also make these methods no longer need suspend
    suspend fun parse(body: String): WeekPlan {
        val shiftTable = Jsoup.parse(body)
            .body()
            .select("#mstatus .prettygrey")
            .get(0)

        return WeekPlan(
            weekday(shiftTable, WeekdayTable.Monday),
            weekday(shiftTable, WeekdayTable.Tuesday),
            weekday(shiftTable, WeekdayTable.Wednesday),
            weekday(shiftTable, WeekdayTable.Thursday),
            weekday(shiftTable, WeekdayTable.Friday),
            weekday(shiftTable, WeekdayTable.Saturday),
            weekday(shiftTable, WeekdayTable.Sunday),
        )
    }

    suspend fun AxpShift.shift(): Shift {
        val helperBooking = axpHelperBooking.toHelperBooking(helperRepository)
        val storedShiftId = axpToDomainMapper.axpBookingToShiftId(bookingId)

        val shiftId = when (storedShiftId) {
            is Either.Right -> storedShiftId.value
            is Either.Left -> {
                val newId = ShiftId.generateId()
                axpToDomainMapper.saveAxpBookingToShiftId(bookingId, newId)
                newId
            }
        }

        return Shift(
            helperBooking,
            shiftId,
            YearWeekDayAtTime.from(start),
            YearWeekDayAtTime.from(end),
        )
    }

    suspend fun weekday(shiftTable: Element, weekday: WeekdayTable): Weekday {
        val day = shiftTable
            .shiftData(ShiftsField.Day, weekday)
            .map { it.shift() }
        val evening = shiftTable
            .shiftData(ShiftsField.Evening, weekday)
            .map { it.shift() }
        val night = shiftTable
            .shiftData(ShiftsField.Night, weekday)
            .map { it.shift() }
        val allDay = shiftTable
            .shiftData(ShiftsField.AllDay, weekday)
            .map { it.shift() }
        val long = shiftTable
            .shiftData(ShiftsField.Long, weekday)
            .map { it.shift() }
        // TODO illness needs to be treated differently, for instance illness does not have a booking id it seems
//    val illness = shiftTable
//        .shiftData(ShiftsField.Illness, weekday)
        val illness = emptyList<AxpShift>()
            .map { it.shift() }

        return Weekday(
            day,
            evening,
            night,
            allDay,
            long,
            illness,
        )
    }
}


private val shiftDataParser = ShiftDataParser()
private fun Element.shiftData(shiftsField: ShiftsField, weekday: WeekdayTable): List<AxpShift> =
    this
        .select("tr:eq(${shiftsField.index}) > td:eq(${weekday.index})")
        .first()
        ?.children()
        ?.let { shiftDataParser.parse(it) }
        ?: throw IllegalStateException("Missing element for $weekday -> $shiftsField")
