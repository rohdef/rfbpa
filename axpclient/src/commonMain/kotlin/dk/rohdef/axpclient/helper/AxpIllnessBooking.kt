package dk.rohdef.axpclient.helper

sealed interface AxpIllnessBooking {
    data class PermanentHelper(val helperTid: HelperTID) : AxpIllnessBooking

    object VacancyBooking : AxpIllnessBooking
}