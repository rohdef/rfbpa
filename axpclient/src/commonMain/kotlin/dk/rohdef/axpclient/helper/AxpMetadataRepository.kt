package dk.rohdef.axpclient.helper

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.helperplanning.shifts.HelperBooking

sealed interface AxpMetadataRepository {
    data class PermanentHelper(val helperId: HelperTID) : AxpMetadataRepository

    object VacancyBooking : AxpMetadataRepository

    object NoBooking : AxpMetadataRepository

    fun toHelperBooking(helperRepository: AxpRepository): HelperBooking {
        return when(this) {
            is PermanentHelper -> HelperBooking.PermanentHelper(helperId.toId(helperRepository))
            is VacancyBooking -> HelperBooking.VacancyHelper
            is NoBooking -> HelperBooking.NoBooking
        }
    }
}
