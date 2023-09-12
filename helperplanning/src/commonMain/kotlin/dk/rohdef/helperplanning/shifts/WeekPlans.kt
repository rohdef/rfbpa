package dk.rohdef.helperplanning.shifts

import arrow.core.toNonEmptyListOrNull

data class WeekPlans(val weekPlans: List<WeekPlan>) {
    val allShifts = weekPlans
        .map { it.allShifts }
        .filterIsInstance<ShiftData.Shifts>()
        .reduce { accumulator, shifts -> accumulator + shifts }

    val nonBookedShifts = allShifts
        .shifts
        .filter { it.helperId is HelperBooking.NoBooking }

    val nonBookedShiftsByDate = nonBookedShifts
        .groupBy { it.start.date }

    fun shiftsFor(helper: HelperBooking.PermanentHelper): ShiftData {
        return allShifts.shifts
            .filter { it.helperId == helper }
            .toNonEmptyListOrNull()
            ?.let { ShiftData.Shifts(it) }
            ?: ShiftData.NoData
    }
}