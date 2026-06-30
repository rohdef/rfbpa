@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.helpers

import dk.rohdef.helperplanning.helpers.Helper
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class HelperDto(
    val id: Uuid,
    val name: String,
) {
    companion object {
        fun from(helper: Helper): HelperDto {
            return HelperDto(
                helper.id.value,
                helper.name,
            )
        }
    }
}