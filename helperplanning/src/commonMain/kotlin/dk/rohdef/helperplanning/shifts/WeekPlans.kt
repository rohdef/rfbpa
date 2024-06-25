package dk.rohdef.helperplanning.shifts

data class WeekPlans(val weekPlans: List<WeekPlan>) {
    val allShifts = weekPlans
        .flatMap { it.allShifts }

    val nonBookedShifts = allShifts
        .filter { it.helperId is HelperBooking.NoBooking }

    val nonBookedShiftsByDate = nonBookedShifts
        .groupBy { it.start.date }
}
