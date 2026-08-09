@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.salary_helper

import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class SalaryHelper(
    val helperId: Uuid,
    val name: String,
    val helperNumber: String,
    val mithfId: String,
    val email: String,
) {
    fun toHelper(): Helper = Helper(HelperId(helperId), name, name)
}