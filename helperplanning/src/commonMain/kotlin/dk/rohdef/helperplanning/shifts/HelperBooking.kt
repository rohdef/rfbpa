package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId

sealed interface HelperBooking {
    object NoBooking : HelperBooking

    // TODO: 31/07/2024 rohdef - probably change to full helper object
    data class PermanentHelper(val helper: HelperId) : HelperBooking

    data class UnknownHelper(val externalReference: String) : HelperBooking

    object VacancyHelper : HelperBooking
}
