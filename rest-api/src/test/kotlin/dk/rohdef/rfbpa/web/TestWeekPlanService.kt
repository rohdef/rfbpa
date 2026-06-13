package dk.rohdef.rfbpa.web

import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceImplementation
import dk.rohdef.rfbpa.web.persistance.TestDatabaseConnection
import dk.rohdef.rfbpa.web.persistance.helpers.DatabaseHelpers
import dk.rohdef.rfbpa.web.persistance.shifts.DatabaseShifts

// TODO consider if we even need a test service any more
class TestWeekPlanService(
    val salarySystem: TestSalarySystemRepository = TestSalarySystemRepository(),
    val shiftRepository: ShiftRepository = DatabaseShifts(),
    val helpersRepository: HelpersRepository = DatabaseHelpers(),
    val synchronizationRepository: TestWeekSynchronizationRepository = TestWeekSynchronizationRepository(),
) : WeekPlanService by WeekPlanServiceImplementation(salarySystem, shiftRepository, helpersRepository, synchronizationRepository) {
    internal fun initialize() {
        TestDatabaseConnection.init()
    }

    internal fun reset() {
        salarySystem.reset()
        synchronizationRepository.reset()
    }

    suspend fun addShift(subject: RfbpaPrincipal.Subject, shift: Shift) {
        salarySystem.addShift(subject, shift)
        shiftRepository.createOrUpdate(subject, shift)
    }
}
