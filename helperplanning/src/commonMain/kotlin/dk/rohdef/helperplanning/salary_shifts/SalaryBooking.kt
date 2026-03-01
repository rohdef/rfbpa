package dk.rohdef.helperplanning.salary_shifts

import dk.rohdef.helperplanning.helpers.HelperId

sealed interface SalaryBooking {
    object NoBooking : SalaryBooking {
        override fun toString(): String = "NoBooking"
    }

    @JvmInline
    value class Helper(val helper: HelperId) : SalaryBooking

    @JvmInline
    value class UnknownHelper(val helper: HelperId) : SalaryBooking

    object Vacancy : SalaryBooking {
        override fun toString(): String = "Vacancy"
    }
}
