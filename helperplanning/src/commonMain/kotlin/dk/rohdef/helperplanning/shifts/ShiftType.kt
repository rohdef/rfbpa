package dk.rohdef.helperplanning.shifts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ShiftType {
    @SerialName("day") DAY,
    @SerialName("evening") EVENING,
    @SerialName("night") NIGHT,
    @SerialName("long") LONG,
    @SerialName("24 hours") HOURS_24,
}
