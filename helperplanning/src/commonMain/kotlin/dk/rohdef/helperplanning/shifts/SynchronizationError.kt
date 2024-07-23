package dk.rohdef.helperplanning.shifts

import dk.rohdef.rfweeks.YearWeek

sealed interface SynchronizationError {
    val yearWeek: YearWeek

    object Dummy: SynchronizationError {
        override val yearWeek: YearWeek
            get() = TODO("not implemented")
    }

    data class CouldNotSynchronizeWeek(
        override val yearWeek: YearWeek,
    ) : SynchronizationError
}
