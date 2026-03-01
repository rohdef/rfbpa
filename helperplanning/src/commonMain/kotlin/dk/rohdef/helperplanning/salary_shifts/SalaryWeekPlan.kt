package dk.rohdef.helperplanning.salary_shifts

import dk.rohdef.rfweeks.YearWeek
import kotlinx.datetime.DayOfWeek

data class SalaryWeekPlan(
    val week: YearWeek,

    val monday: List<SalaryShift>,
    val tuesday: List<SalaryShift>,
    val wednesday: List<SalaryShift>,
    val thursday: List<SalaryShift>,
    val friday: List<SalaryShift>,
    val saturday: List<SalaryShift>,
    val sunday: List<SalaryShift>,
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
        fun emptyPlan(yearWeek: YearWeek) = SalaryWeekPlan(
            yearWeek,
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
        )

        fun unsafeFromList(yearWeek: YearWeek, shifts: List<SalaryShift>): SalaryWeekPlan {
            val shiftInWrongWeek = shifts.filter { it.start.yearWeek != yearWeek }
            if (shiftInWrongWeek.isNotEmpty()) { throw IllegalArgumentException("Shifts in wrong week nw allowed:\nweek expected $yearWeek\nbad shifts:$shiftInWrongWeek\nshifts given:$shifts") }

            return SalaryWeekPlan(
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
