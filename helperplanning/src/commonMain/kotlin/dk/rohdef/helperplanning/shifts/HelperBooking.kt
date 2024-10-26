package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.helpers.Helper

sealed interface HelperBooking {
    object NoBooking : HelperBooking {
        override fun toString(): String = "NoBooking"
    }

    @JvmInline
    value class Booked(val helper: Helper) : HelperBooking
}
