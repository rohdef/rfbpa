package dk.rohdef.axpclient

import dk.rohdef.helperplanning.shifts.YearWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal class AxpUrls(host: String) {
    val base = "https://$host/citizen_web"
    val index = "$base/index.php"
    val indexUWeb = "https://$host/index.php"
    val login = "$base/login.php"

    private val shiftsBase = index +
            "?act=shift_plan"
    fun shiftsForWeek(yearWeek: YearWeek): String {
        val weekStart = yearWeek
            .firstDayEpoch(TimeZone.of("Europe/Copenhagen"))
        val shifts = shiftsBase + "&axp_startdate=$weekStart"

        return shifts
    }
}

fun YearWeek.firstDayEpoch(zone: TimeZone): Long {
    return this.firstDayOfWeek
        .atStartOfDayIn(zone)
        .epochSeconds
}