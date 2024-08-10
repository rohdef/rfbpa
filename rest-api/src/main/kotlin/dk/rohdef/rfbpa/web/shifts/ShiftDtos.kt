package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class ShiftOut(
    val shiftId: UUID,
    val helperBooking: HelperBookingOut,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
) {
    companion object {
        fun from(shift: Shift): ShiftOut {
            val helperBooking = shift.helperBooking
            val booking = when (helperBooking) {
                HelperBooking.NoBooking -> HelperBookingOut.NoBooking
                is HelperBooking.PermanentHelper -> HelperBookingOut.Booking(
                    helperBooking.helper.id.id,
                    helperBooking.helper.shortName,
                )
                is HelperBooking.UnknownHelper -> TODO()
                HelperBooking.VacancyHelper -> TODO()
            }

            return ShiftOut(
                shift.shiftId.id,
                booking,
                shift.start,
                shift.end,
            )
        }
    }
}

@Serializable
sealed interface HelperBookingOut {
    @Serializable
    @SerialName("NoBooking")
    object NoBooking : HelperBookingOut

    @Serializable
    @SerialName("Booking")
    data class Booking(
        val id: UUID,
        val shortName: String,
    ) : HelperBookingOut
}

@Serializable
data class WeekPlanOut(
    val week: YearWeek,

    val monday: List<ShiftOut>,
    val tuesday: List<ShiftOut>,
    val wednesday: List<ShiftOut>,
    val thursday: List<ShiftOut>,
    val friday: List<ShiftOut>,
    val saturday: List<ShiftOut>,
    val sunday: List<ShiftOut>,
) {
    companion object {
        fun from(weekPlan: WeekPlan): WeekPlanOut = with(weekPlan) {
            WeekPlanOut(
                week,
                monday.map { ShiftOut.from(it) },
                tuesday.map { ShiftOut.from(it) },
                wednesday.map { ShiftOut.from(it) },
                thursday.map { ShiftOut.from(it) },
                friday.map { ShiftOut.from(it) },
                saturday.map { ShiftOut.from(it) },
                sunday.map { ShiftOut.from(it) },
            )
        }
    }
}
