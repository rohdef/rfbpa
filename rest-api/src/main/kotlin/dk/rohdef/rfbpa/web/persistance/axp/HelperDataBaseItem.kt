package dk.rohdef.rfbpa.web.persistance.axp

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

// TODO: 25/06/2024 rohdef - delete once proper database actions available
@Serializable
data class HelperDataBaseItem(
    val helperTid: String,
    val helperNumber: String,
    val id: UUID,
)
