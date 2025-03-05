package dk.rohdef.rfbpa.web.persistance.axp

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// TODO: 25/06/2024 rohdef - delete once proper database actions available
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class HelperDataBaseItem(
    val helperTid: String,
    val helperNumber: String,
    val id: Uuid,
)
