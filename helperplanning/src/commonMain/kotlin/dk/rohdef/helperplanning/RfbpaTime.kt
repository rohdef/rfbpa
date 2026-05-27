package dk.rohdef.helperplanning

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RfbpaTime<T : Clock>(
    val clock: T,
    val timeZone: TimeZone = TimeZone.of("Europe/Copenhagen"),
) {
    fun localDateTime(): LocalDateTime {
        return clock.now().toLocalDateTime(timeZone)
    }

    fun localDate(): LocalDate {
        return localDateTime().date
    }

    fun localTime(): LocalTime {
        return localDateTime().time
    }

    fun instant(): Instant {
        return clock.now()
    }
}
