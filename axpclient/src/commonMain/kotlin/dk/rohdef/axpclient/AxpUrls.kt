package dk.rohdef.axpclient

import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal class AxpUrls(
    host: String,
    private val timeZone: TimeZone,
) {
    val base = "$host/citizen_web"
    val index = "$base/index.php"
    val indexUWeb = "$host/index.php"
    val login = "$base/login.php"

    private val shiftsBase = index +
            "?getting_popped=1&act=shift_plan"
    fun shiftsForWeek(yearWeek: YearWeek): String {
        val weekStart = yearWeek
            .firstDayEpoch(timeZone)
        val shifts = shiftsBase + "&axp_startdate=$weekStart"

        return shifts
    }

    fun helperForShift(bookingNumber: String): String {
        val shift = shiftsBase + "&sub_act=search_results&booking=$bookingNumber"

        return shift
    }
}

fun YearWeek.firstDayEpoch(zone: TimeZone): Long {
    return this.monday
        .firstDayEpoch(zone)
}

fun YearWeekDay.firstDayEpoch(zone: TimeZone): Long {
    return this.date
        .atStartOfDayIn(zone)
        .epochSeconds
}
