package dk.rohdef.helperplanning.shifts

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


class FixedClock(
    var fixedInstant: Instant = Instant.fromEpochMilliseconds(0)
) : Clock {
    override fun now(): Instant = fixedInstant
}