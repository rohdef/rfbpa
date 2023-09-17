package dk.rohdef.axpclient

import dk.rohdef.helperplanning.shifts.YearWeek
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone

class WeekDateCalculation  : FunSpec({
    test("canCalculateWeekdate") {
        YearWeek(2023, 23)
            .firstDayEpoch(TimeZone.of("Europe/Copenhagen"))
            .shouldBe(1685916000)
    }
})