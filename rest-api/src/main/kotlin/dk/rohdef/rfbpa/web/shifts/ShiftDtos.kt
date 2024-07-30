package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.LocalDateTime
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
        fun from(shift: Shift) = ShiftOut(
            shift.shiftId.id,
            HelperBookingOut.NoBooking,
            shift.start,
            shift.end,
        )
    }
}

@Serializable
sealed interface HelperBookingOut {
    object NoBooking : HelperBookingOut
}

@Serializable
data class WeekPlanOut(
    val week: String,

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
                week.toString(),
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
