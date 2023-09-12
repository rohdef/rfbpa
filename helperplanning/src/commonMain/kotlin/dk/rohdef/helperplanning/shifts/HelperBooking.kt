package dk.rohdef.helperplanning.shifts

sealed interface HelperBooking {
    object NoBooking : HelperBooking

    // TODO strong class once helper structure there
    data class PermanentHelper(val id: String) : HelperBooking

    data class VacancyHelper(
        val name: String,
        val phone: String,
    ) : HelperBooking
}