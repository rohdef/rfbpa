package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.helpers.Helper

sealed interface HelperBooking {
    object NoBooking : HelperBooking

    data class PermanentHelper(val helper: Helper) : HelperBooking

    data class UnknownHelper(val externalReference: String) : HelperBooking

    object VacancyHelper : HelperBooking
}
