package dk.rohdef.helperplanning.helpers

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class HelperId(
    val id: Uuid,
) {
    companion object {
        fun generateId(): HelperId {
            return HelperId(Uuid.random())
        }
    }
}
