package dk.rohdef.axpclient.helper

sealed interface AxpHelperBooking {
    data class PermanentHelper(val helperNumber: HelperNumber) : AxpHelperBooking

    object VacancyBooking : AxpHelperBooking

    object NoBooking : AxpHelperBooking
}
