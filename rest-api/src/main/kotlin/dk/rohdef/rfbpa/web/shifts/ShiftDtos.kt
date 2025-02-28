@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.helperplanning.toKotlinxUUID
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.toKotlinUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid

@Serializable
data class ShiftOut(
    val shiftId: UUID,
    val helperBooking: HelperBookingOut,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
) {
    companion object {
        fun from(shift: Shift, helpers: Map<HelperId, Helper>): ShiftOut {
            val helperBooking = shift.helperBooking
            val booking = when (helperBooking) {
                HelperBooking.NoBooking -> HelperBookingOut.NoBooking
                is HelperBooking.Booked -> HelperBookingOut.Booking.from(helpers.getValue(helperBooking.helper))
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
        val name: String,
    ) : HelperBookingOut {
        companion object {
            fun from(helper: Helper) = Booking(
                helper.id.id,
                helper.name,
            )
        }
    }
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
        fun from(weekPlan: WeekPlan, helpers: Map<HelperId, Helper>): WeekPlanOut = with(weekPlan) {
            WeekPlanOut(
                week,
                monday.map { ShiftOut.from(it, helpers) },
                tuesday.map { ShiftOut.from(it, helpers) },
                wednesday.map { ShiftOut.from(it, helpers) },
                thursday.map { ShiftOut.from(it, helpers) },
                friday.map { ShiftOut.from(it, helpers) },
                saturday.map { ShiftOut.from(it, helpers) },
                sunday.map { ShiftOut.from(it, helpers) },
            )
        }
    }
}
