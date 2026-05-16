@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.MemoryHelpersRepository
import dk.rohdef.helperplanning.generateUuid
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.yaml.RfbpaBooking
import dk.rohdef.helperplanning.shifts.yaml.SalaryBooking
import dk.rohdef.helperplanning.shifts.yaml.Shifties
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HelperMananger private constructor(
    val helpers: MemoryHelpersRepository,
    val unknownHelpers: Map<String, HelperId>,
    val shiftIdToHelper: Map<ShiftId, HelperId>,
) {
    companion object {
        private val uuidNamespace = Uuid.random()

        suspend fun create(shifties: Shifties): Either<Unit, HelperMananger> = either {
            val helpers = MemoryHelpersRepository()
            val shiftIdToHelper = mutableMapOf<ShiftId, HelperId>()

            val allShifts = shifties.inSystem
                .flatMap { it.value }

            val knownHelpers = (shifties.rfbpaBookingsHelper.map { it.helper } +
                    shifties.salaryBookingsHelper.map { it.helper })
                .toSet()
                .map {
                    Helper(
                    Uuid.generateUuid(uuidNamespace, it).let { HelperId(it) },
                        it,
                        it,
                    )
                }
            knownHelpers.forEach { helpers.create(it) }

            val unknownSalaryHelpers = shifties.salaryBookingsUnknown.map { it.helper }
            val unknownHelpers = unknownSalaryHelpers.associateWith { HelperId.generateId() }

            for (shift in allShifts) {
                val booking = shift.salaryBooking
                val helperId = HelperId.generateId()

                when (booking) {
                    is SalaryBooking.Vacancy -> when (shift.rfbpaBooking) {
                        is RfbpaBooking.Helper -> knownHelpers.find { it.shortName == shift.rfbpaBooking.helper }
                            ?: unknownHelpers[shift.rfbpaBooking.helper]
                            ?: throw IllegalStateException("Helper not found for shift ${shift.testId}")

                        RfbpaBooking.NotBooked -> shiftIdToHelper[ShiftId(shift.testId)] = helperId
                        null -> shiftIdToHelper[ShiftId(shift.testId)] = helperId
                    }

                    else -> {}
                }
            }

            HelperMananger(helpers, unknownHelpers, shiftIdToHelper)
        }
    }
}