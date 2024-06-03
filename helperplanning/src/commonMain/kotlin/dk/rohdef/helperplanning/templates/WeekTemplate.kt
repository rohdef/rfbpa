package dk.rohdef.helperplanning.templates

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek.*

@Serializable
data class WeekTemplate(
    val name: String,
    val shifts: Map<@Serializable(DayOfWeekSerializer::class) DayOfWeek, List<ShiftTemplate>>
) {
    class DayOfWeekSerializer : KSerializer<DayOfWeek> {
        override val descriptor = PrimitiveSerialDescriptor("DayOfWeek", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): DayOfWeek {
            val text = decoder.decodeString()

            return text.uppercase().let { DayOfWeek.valueOf(it) }
        }

        override fun serialize(encoder: Encoder, value: DayOfWeek) {
            when (value) {
                MONDAY -> "Monday"
                TUESDAY -> "Tuesday"
                WEDNESDAY -> "Wednesday"
                THURSDAY -> "Thursday"
                FRIDAY -> "Friday"
                SATURDAY -> "Saturday"
                SUNDAY -> "Sunday"
            }.apply { encoder.encodeString(this) }
        }
    }
}
