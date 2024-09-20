package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.rfweeks.YearWeek

sealed interface SynchronizationError {
    data class CouldNotSynchronizeWeek(
        val yearWeek: YearWeek,
    ) : SynchronizationError

    data class InsufficientPermissions(
        val expectedRole: RfbpaPrincipal.RfbpaRoles,
        val actualRoles: Set<RfbpaPrincipal.RfbpaRoles>,
    ) : SynchronizationError
}
