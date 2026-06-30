@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.axpclient.helper

import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AxpHelper(
    val id: Uuid,
    val tid: HelperTID,
    val number: HelperNumber,
    val name: String,
    val shortName: String,
) {
    fun toHelper(): Helper = Helper(HelperId(id), name, shortName)
}
