package dk.rohdef.helperplanning.helpers

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class HelperId(
    val value: Uuid,
) {
    companion object {
        fun generateId(): HelperId {
            return HelperId(Uuid.random())
        }

        fun fromString(uuidString: String): HelperId {
            return HelperId(Uuid.parse(uuidString))
        }
    }
}
