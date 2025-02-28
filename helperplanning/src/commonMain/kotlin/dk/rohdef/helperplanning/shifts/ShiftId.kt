@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.shifts

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class ShiftId(
    val id: Uuid,
) {
    companion object {
        fun generateId(): ShiftId {
            return ShiftId(Uuid.random())
        }
    }
}
