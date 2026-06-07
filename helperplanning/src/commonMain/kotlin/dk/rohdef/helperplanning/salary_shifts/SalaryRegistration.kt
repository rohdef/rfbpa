package dk.rohdef.helperplanning.salary_shifts

import dk.rohdef.helperplanning.shifts.Registration

sealed interface SalaryRegistration {
    fun toRegistration(): Registration

    object Illness : SalaryRegistration {
        override fun toRegistration() = Registration.Illness
    }
}