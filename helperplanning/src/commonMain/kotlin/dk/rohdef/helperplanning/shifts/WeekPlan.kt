package dk.rohdef.helperplanning.shifts

import dk.rohdef.rfweeks.YearWeek
import kotlinx.datetime.DayOfWeek

data class WeekPlan(
    val week: YearWeek,

    val monday: List<Shift>,
    val tuesday: List<Shift>,
    val wednesday: List<Shift>,
    val thursday: List<Shift>,
    val friday: List<Shift>,
    val saturday: List<Shift>,
    val sunday: List<Shift>,
) {
    val allDays = listOf(
        monday,
        tuesday,
        wednesday,
        thursday,
        friday,
        saturday,
        sunday,
    )

    val allShifts =
        allDays.flatMap { it }

    companion object {
        fun emptyPlan(yearWeek: YearWeek) = WeekPlan(
            yearWeek,
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
        )

        fun unsafeFromList(yearWeek: YearWeek, shifts: List<Shift>): WeekPlan {
            val shiftInWrongWeek = shifts.filter { it.start.yearWeek != yearWeek }
            if (shiftInWrongWeek.isNotEmpty()) { throw IllegalArgumentException("Shifts in wrong week nw allowed:\nweek expected $yearWeek\nbad shifts:$shiftInWrongWeek\nshifts given:$shifts") }

            return WeekPlan(
                yearWeek,
                shifts.filter { it.start.dayOfWeek == DayOfWeek.MONDAY },
                shifts.filter { it.start.dayOfWeek == DayOfWeek.TUESDAY },
                shifts.filter { it.start.dayOfWeek == DayOfWeek.WEDNESDAY },
                shifts.filter { it.start.dayOfWeek == DayOfWeek.THURSDAY },
                shifts.filter { it.start.dayOfWeek == DayOfWeek.FRIDAY },
                shifts.filter { it.start.dayOfWeek == DayOfWeek.SATURDAY },
                shifts.filter { it.start.dayOfWeek == DayOfWeek.SUNDAY },
            )
        }
    }
}
