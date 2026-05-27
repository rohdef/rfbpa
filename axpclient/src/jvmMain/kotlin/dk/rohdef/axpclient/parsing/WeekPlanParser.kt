package dk.rohdef.axpclient.parsing

import dk.rohdef.axpclient.AxpBookingId
import dk.rohdef.axpclient.helper.AxpIllnessBooking
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.axpclient.shift.AxpIllnessShift
import dk.rohdef.axpclient.shift.AxpShift
import dk.rohdef.axpclient.shift.WeekPlan
import dk.rohdef.axpclient.shift.Weekday
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal class WeekPlanParser {
    private val shiftParser = ShiftParser()

    suspend fun parse(body: String, helperBooking: suspend (AxpBookingId) -> AxpIllnessBooking): WeekPlan {
        val shiftTable = Jsoup.parse(body)
            .body()
            .select("#mstatus .prettygrey")
            .get(0)

        return WeekPlan(
            weekday(shiftTable, WeekdayTable.Monday, helperBooking),
            weekday(shiftTable, WeekdayTable.Tuesday, helperBooking),
            weekday(shiftTable, WeekdayTable.Wednesday, helperBooking),
            weekday(shiftTable, WeekdayTable.Thursday, helperBooking),
            weekday(shiftTable, WeekdayTable.Friday, helperBooking),
            weekday(shiftTable, WeekdayTable.Saturday, helperBooking),
            weekday(shiftTable, WeekdayTable.Sunday, helperBooking),
        )
    }

    suspend fun weekday(shiftTable: Element, weekday: WeekdayTable, helperBooking: suspend (AxpBookingId) -> AxpIllnessBooking): Weekday {
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
        val illness = shiftTable
            .illnessData(ShiftsField.Illness, weekday, helperBooking)

        return Weekday(
            day,
            evening,
            night,
            allDay,
            long,
            illness,
        )
    }

    fun illnessBooking(body: String): AxpIllnessBooking {
        val checkedCheckbox = Jsoup.parse(body)
            .body()
            .select(".citizen_search_result_container input[type=\"checkbox\"]")
            .first { it.hasAttr("checked") }!! // TODO Better execption/error

        val tid = checkedCheckbox.bookinData()

        return when(tid) {
            "92505" -> AxpIllnessBooking.VacancyBooking
            "92580" -> AxpIllnessBooking.VacancyBooking
            else -> AxpIllnessBooking.PermanentHelper(HelperTID(tid))
        }
    }

    private fun Element.bookinData(): String =
        this.`val`()
            .trim()
            .let {
                if (it.isBlank()) throw IllegalArgumentException("Helper TID cannot be blank")
                else it
            }

    private fun Element.shiftData(shiftsField: ShiftsField, weekday: WeekdayTable): List<AxpShift> =
        this
            .select("tr:eq(${shiftsField.index}) > td:eq(${weekday.index})")
            .first()
            ?.children()
            ?.let { it.map { element -> shiftParser.parseShift(element) } }
            ?: throw IllegalStateException("Missing element for $weekday -> $shiftsField")

    private suspend fun Element.illnessData(shiftsField: ShiftsField, weekday: WeekdayTable, helperBooking: suspend (AxpBookingId) -> AxpIllnessBooking): List<AxpIllnessShift> =
        this
            .select("tr:eq(${shiftsField.index}) > td:eq(${weekday.index})")
            .first()
            ?.children()
            ?.let { it.map { element -> shiftParser.pill(element, helperBooking) } }
            ?: throw IllegalStateException("Missing element for $weekday -> $shiftsField")
}
