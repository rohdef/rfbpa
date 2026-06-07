@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ShiftOut(
    val shiftId: Uuid,
    val helperBooking: HelperBookingOut,
    val start: YearWeekDayAtTime,
    val end: YearWeekDayAtTime,
    val registrations: List<RegistrationOut>,
    val references: List<ReferenceOut>,
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
                shift.registrations.map { RegistrationOut.from(it) },
                shift.references.map { ReferenceOut.from(it) },
            )
        }
    }
}

@Serializable
sealed interface RegistrationOut {
    @Serializable
    @SerialName("Illness")
    object Illness : RegistrationOut

    companion object {
        fun from(registration: Registration): RegistrationOut =
            when (registration) {
                Registration.Illness -> Illness
            }
    }
}

@Serializable
sealed interface ReferenceOut {
    @Serializable
    @SerialName("From")
    data class From(
        val id: Uuid,
        val linkType: LinkType,
    ) : ReferenceOut {
        companion object {
            fun from(link: Reference.From) = From(
                link.id.id,
                LinkType.from(link.linkType),
            )
        }
    }

    @Serializable
    @SerialName("To")
    data class To(
        val id: Uuid,
        val linkType: LinkType,
    ) : ReferenceOut {
        companion object {
            fun from(link: Reference.To) = To(
                link.id.id,
                LinkType.from(link.linkType),
            )
        }
    }

    enum class LinkType {
        Illness;

        companion object {
            fun from(link: Reference.LinkType): LinkType =
                when (link) {
                    Reference.LinkType.ILLNESS -> Illness
                }
        }
    }

    companion object {
        fun from(reference: Reference) =
            when (reference) {
                is Reference.From -> From.from(reference)
                is Reference.To -> To.from(reference)
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
        val id: Uuid,
        val name: String,
    ) : HelperBookingOut {
        companion object {
            fun from(helper: Helper) = Booking(
                helper.id.value,
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
