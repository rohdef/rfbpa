package dk.rohdef.axpclient

import dk.rohdef.helperplanning.shifts.YearWeek
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone
import kotlin.test.Test

class WeekDateCalculation {
    private val axpUrls = AxpUrls("")

    @Test
    fun canCalculateWeekdate() {
        YearWeek(2023, 23)
            .firstDayEpoch(TimeZone.of("Europe/Copenhagen"))
            .shouldBe(1685916000)
    }
}