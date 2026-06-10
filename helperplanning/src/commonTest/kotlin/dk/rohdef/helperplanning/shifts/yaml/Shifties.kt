@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts.yaml

import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.salary_shifts.SalaryShift
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeek
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class Shifties(
    val inSystem: Map<YearWeek, List<InSystemShift>>,
    val synchronizationStates: Map<YearWeek, SynchronizationState>,
) {
    private typealias HelperResolver = suspend (String)-> HelperId

    suspend fun salaryShifts(helperResolver: HelperResolver): List<SalaryShift> {
        return inSystem.flatMap { (yearWeek, shifts) ->
            shifts
                .filter { it.salaryBooking != null }
                .map { shift ->
                    SalaryShift(
                        shift.salaryBooking!!.asSalaryBooking(helperResolver),
                        ShiftId(shift.testId),
                        yearWeek.atDayOfWeek(shift.day).atTime(shift.time.start),
                        yearWeek.atDayOfWeek(shift.day).atTime(shift.time.end),
                        shift.registrations.map { TODO("Needs to be fixed when introducing registrations") },
                    )
                }
        }
    }

    suspend fun rfbpaShifts(helperResolver: HelperResolver): List<Shift> {
        return inSystem.flatMap { (yearWeek, shifts) ->
            shifts
                .filter { it.rfbpaBooking != null }
                .map { shift ->
                    Shift.createUnsafe(
                        shift.rfbpaBooking!!.asHelperBooking(helperResolver),
                        ShiftId(shift.testId),
                        yearWeek.atDayOfWeek(shift.day).atTime(shift.time.start),
                        yearWeek.atDayOfWeek(shift.day).atTime(shift.time.end),
                        shift.registrations.map { TODO("Needs to be fixed when introducing registrations") },
                        shift.registrations.map { TODO("Needs to be fixed when introducing registrations") },
                    )
                }
        }
    }

    suspend fun rfbpaShiftsMissing(helperResolver: HelperResolver, helperByShiftId: (ShiftId) -> HelperId): List<Shift> {
        return inSystem.flatMap { (yearWeek, shifts) ->
            shifts
                .filter { it.rfbpaBooking == null }
                .filter { it.salaryBooking != null }
                .map { shift ->
                    val shiftId = ShiftId(shift.testId)
                    Shift.createUnsafe(
                        shift.salaryBooking!!.asExpectedHelperBooking(helperResolver, helperByShiftId(shiftId)),
                        shiftId,
                        yearWeek.atDayOfWeek(shift.day).atTime(shift.time.start),
                        yearWeek.atDayOfWeek(shift.day).atTime(shift.time.end),
                        shift.registrations.map { TODO("Needs to be fixed when introducing registrations") },
                        shift.registrations.map { TODO("Needs to be fixed when introducing registrations") },
                    )
                }
        }
    }

    fun helpers(): Set<String> {
        return inSystem.flatMap { (_, shifts) ->
            shifts
                .map { it.salaryBooking?.helperShortName() }
                .filterNotNull()
        }.toSet()
    }

    val allShifts = inSystem.flatMap { it.value }

    val salaryBookings = allShifts
        .map { it.salaryBooking }
    val salaryBookingsHelper = salaryBookings
        .filterIsInstance<SalaryBooking.Helper>()
    val salaryBookingsUnknown = salaryBookings
        .filterIsInstance<SalaryBooking.Unknown>()
    val salaryBookingsVacancy = salaryBookings
        .filterIsInstance<SalaryBooking.Vacancy>()
    val salaryBookingsNotBooked = salaryBookings
        .filterIsInstance<SalaryBooking.NotBooked>()

    val rfbpaBookings = allShifts
        .map { it.rfbpaBooking }
    val rfbpaBookingsHelper = rfbpaBookings
        .filterIsInstance<RfbpaBooking.Helper>()
    val rfbpaBookingsNotBooked = rfbpaBookings
        .filterIsInstance<RfbpaBooking.NotBooked>()

}
