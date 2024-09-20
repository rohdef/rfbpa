package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.rfweeks.YearWeek

sealed interface SynchronizationError {
    val yearWeek: YearWeek

    data class CouldNotSynchronizeWeek(
        override val yearWeek: YearWeek,
    ) : SynchronizationError

    data class InsufficientPermissions(
        override val yearWeek: YearWeek,
        val expectedRole: RfbpaPrincipal.RfbpaRoles,
        val actualRoles: Set<RfbpaPrincipal.RfbpaRoles>,
    ) : SynchronizationError
}
