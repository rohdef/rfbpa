package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.helpers.Helper

sealed interface HelperBooking {
    object NoBooking : HelperBooking {
        override fun toString(): String = "NoBooking"
    }

    @JvmInline
    value class PermanentHelper(val helper: Helper) : HelperBooking

    @JvmInline
    value class UnknownHelper(val externalReference: String) : HelperBooking

    object VacancyHelper : HelperBooking
}
