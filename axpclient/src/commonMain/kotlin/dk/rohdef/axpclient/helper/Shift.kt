package dk.rohdef.axpclient.helper

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.rfweeks.YearWeekDayAtTime
import dk.rohdef.helperplanning.shifts.Shift as DomainShift
import kotlinx.datetime.LocalDateTime

internal data class Shift(
    val axpHelperBooking: AxpMetadataRepository,
    val axpShiftId: AxpShiftId,
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    data class AxpShiftId(val id: String)

    fun shift(axpRepository: AxpRepository) = DomainShift(
        axpHelperBooking.toHelperBooking(axpRepository),
        axpRepository.shiftIdByBookingNumber(axpShiftId.id),
        YearWeekDayAtTime.from(start),
        YearWeekDayAtTime.from(end),
    )
}
