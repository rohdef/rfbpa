package dk.rohdef.helperplanning.helpers

import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

@JvmInline
value class HelperId(
    val id: UUID,
) {
    companion object {
        fun generateId(): HelperId {
            return HelperId(UUID.generateUUID())
        }
    }
}
