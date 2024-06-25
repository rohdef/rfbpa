package dk.rohdef.axpclient.parsing

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.helper.Shift
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.helperplanning.shifts.Weekday
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal class WeekPlanParser(
    val helperRepository: AxpRepository,
) {
    fun parse(body: String): WeekPlan {
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

    fun weekday(shiftTable: Element, weekday: WeekdayTable): Weekday {
        val day = shiftTable
            .shiftData(ShiftsField.Day, weekday)
            .map { it.shift(helperRepository) }
        val evening = shiftTable
            .shiftData(ShiftsField.Evening, weekday)
            .map { it.shift(helperRepository) }
        val night = shiftTable
            .shiftData(ShiftsField.Night, weekday)
            .map { it.shift(helperRepository) }
        val allDay = shiftTable
            .shiftData(ShiftsField.AllDay, weekday)
            .map { it.shift(helperRepository) }
        val long = shiftTable
            .shiftData(ShiftsField.Long, weekday)
            .map { it.shift(helperRepository) }
        // TODO illness needs to be treated differently, for instance illness does not have a booking id it seems
//    val illness = shiftTable
//        .shiftData(ShiftsField.Illness, weekday)
        val illness = emptyList<Shift>()
            .map { it.shift(helperRepository) }

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
private fun Element.shiftData(shiftsField: ShiftsField, weekday: WeekdayTable): List<Shift> =
    this
        .select("tr:eq(${shiftsField.index}) > td:eq(${weekday.index})")
        .first()
        ?.children()
        ?.let { shiftDataParser.parse(it) }
        ?: throw IllegalStateException("Missing element for $weekday -> $shiftsField")
