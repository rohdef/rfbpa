package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.helpers.Helper

sealed interface HelperBooking {
    object NoBooking : HelperBooking

    data class PermanentHelper(val helperId: Helper.ID) : HelperBooking

    object VacancyHelper : HelperBooking
}
