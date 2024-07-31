package dk.rohdef.axpclient.helper

import arrow.core.Option
import arrow.core.toOption
import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.helperplanning.helpers.HelperId

data class HelperNumber(
    val id: String,
) {
    fun toId(bookingToShiftId: Map<HelperNumber, HelperId>): Option<HelperId> {
        return bookingToShiftId[this].toOption()
    }
}
