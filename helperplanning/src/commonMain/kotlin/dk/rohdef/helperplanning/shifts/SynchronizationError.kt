package dk.rohdef.helperplanning.shifts

import dk.rohdef.rfweeks.YearWeek

sealed interface SynchronizationError {
    val yearWeek: YearWeek

    data class CouldNotSynchronizeWeek(
        override val yearWeek: YearWeek,
    ) : SynchronizationError
}
