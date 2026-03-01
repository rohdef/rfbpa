package dk.rohdef.rfbpa.web

import dk.rohdef.helperplanning.MemoryHelpersRepository
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.shifts.WeekPlanService
import dk.rohdef.helperplanning.shifts.WeekPlanServiceImplementation

// TODO consider if we even need a test service any more
class TestWeekPlanService(
    val salarySystem: TestSalarySystemRepository = TestSalarySystemRepository(),
    val shiftRepository: TestShiftRespository = TestShiftRespository(),
    val helpersRepository: HelpersRepository = MemoryHelpersRepository(),
    val synchronizationRepository: TestWeekSynchronizationRepository = TestWeekSynchronizationRepository(),
) : WeekPlanService by WeekPlanServiceImplementation(salarySystem, shiftRepository, helpersRepository, synchronizationRepository) {
    internal fun reset() {
        salarySystem.reset()
        shiftRepository.reset()
        synchronizationRepository.reset()
    }
}
