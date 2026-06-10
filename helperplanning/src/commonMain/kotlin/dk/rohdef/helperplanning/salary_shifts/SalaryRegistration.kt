package dk.rohdef.helperplanning.salary_shifts

import dk.rohdef.helperplanning.shifts.Registration

enum class SalaryRegistration {
    Illness;

    fun toRegistration(): Registration =
        when (this) {
            Illness -> Registration.Illness
        }
}