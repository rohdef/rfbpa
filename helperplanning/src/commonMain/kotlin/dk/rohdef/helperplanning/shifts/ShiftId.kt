package dk.rohdef.helperplanning.shifts

import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

data class ShiftId(
    val id: UUID,
) {
    companion object {
        fun generateId(): ShiftId {
            return ShiftId(UUID.generateUUID())
        }
    }
}
