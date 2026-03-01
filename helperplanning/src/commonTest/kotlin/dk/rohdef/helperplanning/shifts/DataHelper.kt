@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.TestSalarySystemRepository
import dk.rohdef.helperplanning.TestShiftRespository
import dk.rohdef.helperplanning.TestWeekSynchronizationRepository
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.yaml.Shifties
import dk.rohdef.helperplanning.shifts.yaml.SynchronizationState
import kotlin.uuid.ExperimentalUuidApi

class DataHelper private constructor(
    val shifties: Shifties,
    val subject: RfbpaPrincipal.Subject,
    val helpers: HelperMananger,
    val salarySystem: TestSalarySystemRepository,
    val shiftRepository: TestShiftRespository,
    val weekSynchronizationRepository: TestWeekSynchronizationRepository,
) {
    val helperByShiftId: (ShiftId) -> HelperId = {
        helpers.shiftIdToHelper[it] ?: HelperId.generateId()
    }

    fun reset() {
        salarySystem.reset()
        shiftRepository.reset()
        weekSynchronizationRepository.reset()
    }

    val allHelpersByShortName: suspend (String) -> HelperId = { shortName ->
        helpers.helpers.byShortName(shortName)
            .map { it.id }
            .getOrElse { helpers.unknownHelpers[shortName] }!!
    }

    companion object {
        suspend fun create(
            shifties: Shifties,
            subject: RfbpaPrincipal.Subject,
        ): Either<Unit, DataHelper> = either {
            val salarySystem = TestSalarySystemRepository()
            val shiftRepository = TestShiftRespository()
            val weekSynchronizationRepository = TestWeekSynchronizationRepository()

            val helperMananger = HelperMananger.create(shifties)
                .bind()

            shifties.salaryShifts {
                helperMananger.helpers.byShortName(it)
                    .map { it.id }
                    .getOrElse {
                        helperMananger.unknownHelpers[it.shortName] ?: throw IllegalStateException("Helper not found: ${it.shortName}")
                    }
            }.forEach {
                salarySystem.addShift(subject, it)
            }

            helperMananger.shiftIdToHelper
                .forEach { shiftId, helperId -> shiftRepository.addUpcommingShiftBooking(shiftId, helperId) }

            shifties.rfbpaShifts {
                helperMananger.helpers.byShortName(it)
                    .map { it.id }
                    .getOrElse { throw IllegalStateException("Helper not found: ${it.shortName}") }
            }.forEach {
                shiftRepository.createOrUpdate(subject, it)
                    .mapLeft { }
                    .bind()
            }

            shifties.synchronizationStates.forEach { (week, state) ->
                when (state) {
                    SynchronizationState.SYNCHRONIZED -> weekSynchronizationRepository.markSynchronized(subject, week)
                    SynchronizationState.OUT_OF_SYNC -> weekSynchronizationRepository.markForSynchronization(subject, week)
                }
                    .mapLeft { }
                    .bind()
            }

            DataHelper(
                shifties,
                subject,
                helperMananger,
                salarySystem,
                shiftRepository,
                weekSynchronizationRepository,
            )
        }
    }
}