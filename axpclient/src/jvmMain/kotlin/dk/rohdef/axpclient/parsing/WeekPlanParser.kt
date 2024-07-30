package dk.rohdef.axpclient.parsing

import dk.rohdef.axpclient.shift.AxpShift
import dk.rohdef.axpclient.shift.WeekPlan
import dk.rohdef.axpclient.shift.Weekday
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal class WeekPlanParser {
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
        val evening = shiftTable
            .shiftData(ShiftsField.Evening, weekday)
        val night = shiftTable
            .shiftData(ShiftsField.Night, weekday)
        val allDay = shiftTable
            .shiftData(ShiftsField.AllDay, weekday)
        val long = shiftTable
            .shiftData(ShiftsField.Long, weekday)
        // TODO illness needs to be treated differently, for instance illness does not have a booking id it seems
//    val illness = shiftTable
//        .shiftData(ShiftsField.Illness, weekday)
        val illness = emptyList<AxpShift>()

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
