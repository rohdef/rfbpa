package dk.rohdef.axpclient.helper

sealed interface AxpMetadataRepository {
    data class PermanentHelper(val helperNumber: HelperNumber) : AxpMetadataRepository

    object VacancyBooking : AxpMetadataRepository

    object NoBooking : AxpMetadataRepository
}
