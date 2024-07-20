package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.SalarySystemRepository

class StaticIdSalarySystem(
    val salarySystemRepository: SalarySystemRepository
) : SalarySystemRepository by salarySystemRepository {

}
