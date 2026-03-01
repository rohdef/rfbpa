package dk.rohdef.helperplanning.shifts.yaml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SynchronizationState.Serializer::class)
enum class SynchronizationState {
    SYNCHRONIZED,
    OUT_OF_SYNC;

    class Serializer : KSerializer<SynchronizationState> {
        override val descriptor = PrimitiveSerialDescriptor("SynchronizationState", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): SynchronizationState {
            val text = decoder.decodeString().lowercase().trim()

            return when (text) {
                "synchronized", "synced" -> SYNCHRONIZED
                "out of sync", "out-of-sync", "out_of_sync" -> OUT_OF_SYNC
                else -> throw IllegalArgumentException("Invalid synchronization state: $text")
            }
        }

        override fun serialize(encoder: Encoder, value: SynchronizationState) {
            when (value) {
                SYNCHRONIZED -> "Synchronized"
                OUT_OF_SYNC -> "Out of sync"
            }.apply { encoder.encodeString(this) }
        }
    }
}