package dk.rohdef.rfbpa

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class HelperDataBaseItem(
    val helperTid: String,
    val helperNumber: String,
    val id: UUID,
)
