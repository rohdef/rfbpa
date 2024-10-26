package dk.rohdef.axpclient.helper

import arrow.core.getOrElse
import arrow.core.toOption
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.HelperBooking

sealed interface AxpMetadataRepository {
    data class PermanentHelper(val helperNumber: HelperNumber) : AxpMetadataRepository

    object VacancyBooking : AxpMetadataRepository

    object NoBooking : AxpMetadataRepository

    fun toHelperBooking(
        bookingToHelperId: Map<HelperNumber, HelperId>,
        helpers: Map<HelperId, Helper>,
    ): HelperBooking {
        return when(this) {
            is PermanentHelper -> {
                helperNumber.toId(bookingToHelperId)
                    .flatMap { helpers[it].toOption() }
                    .getOrElse { Helper.Unknown(TODO("Find the anme")) }
                    .let { HelperBooking.Booked(it) }
            }
            is VacancyBooking -> HelperBooking.Booked(Helper.Temp(TODO("Find the name")))
            is NoBooking -> HelperBooking.NoBooking
        }
    }
}
