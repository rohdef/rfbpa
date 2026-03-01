@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts.yaml

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class InSystemShift(
    @Serializable(with = DayOfWeekSerializer::class)
    val day: DayOfWeek,
    val time: TimeInterval,
    val salaryBooking: SalaryBooking? = null,
    val rfbpaBooking: RfbpaBooking? = null,
    val testId: Uuid = Uuid.random(),
    val registrations: List<String> = emptyList(),
)