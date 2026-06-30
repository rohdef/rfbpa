package dk.rohdef.axpclient.helper

sealed interface AxpIllnessBooking {
    data class PermanentHelper(val helperNumber: HelperNumber) : AxpIllnessBooking

    object VacancyBooking : AxpIllnessBooking
}