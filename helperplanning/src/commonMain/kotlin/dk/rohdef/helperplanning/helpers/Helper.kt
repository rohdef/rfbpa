package dk.rohdef.helperplanning.helpers

import kotlinx.uuid.UUID

data class Helper(
    val id: ID,
    val shortName: String,
) {
    data class ID(
        val uuid: UUID,
    )
}
