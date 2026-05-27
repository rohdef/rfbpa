package dk.rohdef.helperplanning.shifts.yaml

import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TimeInterval.Serializer::class)
data class TimeInterval(
    val start: LocalTime,
    val end: LocalTime,
) {
    object Serializer : KSerializer<TimeInterval> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("TimeInterval", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): TimeInterval {
            val text = decoder.decodeString()
            val parts = text.split("-")
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid TimeInterval format: $text. Expected HH:mm-HH:mm")
            }
            return TimeInterval(
                LocalTime.parse(parts[0]),
                LocalTime.parse(parts[1]),
            )
        }

        override fun serialize(encoder: Encoder, value: TimeInterval) {
            val start = value.start.toString().substring(0, 5)
            val end = value.end.toString().substring(0, 5)
            encoder.encodeString("$start-$end")
        }
    }
}
