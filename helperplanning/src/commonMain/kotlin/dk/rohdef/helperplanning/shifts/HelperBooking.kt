package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.helpers.HelperId

sealed interface HelperBooking {
    object NoBooking : HelperBooking {
        override fun toString(): String = "NoBooking"
    }

    @JvmInline
    value class Booked(val helper: HelperId) : HelperBooking
}
