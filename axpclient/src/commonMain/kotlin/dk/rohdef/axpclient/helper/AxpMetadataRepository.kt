package dk.rohdef.axpclient.helper

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.helperplanning.shifts.HelperBooking

sealed interface AxpMetadataRepository {
    data class PermanentHelper(val helperNumber: HelperNumber) : AxpMetadataRepository

    object VacancyBooking : AxpMetadataRepository

    object NoBooking : AxpMetadataRepository

    fun toHelperBooking(helperRepository: AxpRepository): HelperBooking {
        return when(this) {
            is PermanentHelper -> HelperBooking.PermanentHelper(helperNumber.toId(helperRepository))
            is VacancyBooking -> HelperBooking.VacancyHelper
            is NoBooking -> HelperBooking.NoBooking
        }
    }
}
