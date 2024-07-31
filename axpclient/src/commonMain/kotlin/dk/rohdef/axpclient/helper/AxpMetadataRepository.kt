package dk.rohdef.axpclient.helper

import arrow.core.getOrElse
import arrow.core.toOption
import dk.rohdef.axpclient.AxpHelperReferences
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
                    .map { HelperBooking.PermanentHelper(it) }
                    .getOrElse { HelperBooking.UnknownHelper(helperNumber.id) }
            }
            is VacancyBooking -> HelperBooking.VacancyHelper
            is NoBooking -> HelperBooking.NoBooking
        }
    }
}
