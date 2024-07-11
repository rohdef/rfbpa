package dk.rohdef.axpclient

import dk.rohdef.rfweeks.YearWeek
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
            "?act=shift_plan"
    fun shiftsForWeek(yearWeek: YearWeek): String {
        val weekStart = yearWeek
            .firstDayEpoch(timeZone)
        val shifts = shiftsBase + "&axp_startdate=$weekStart"

        return shifts
    }
}

fun YearWeek.firstDayEpoch(zone: TimeZone): Long {
    return this.firstDayOfWeek
        .atStartOfDayIn(zone)
        .epochSeconds
}
