package dk.rohdef.helperplanning.shifts.yaml

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class DayOfWeekSerializer : KSerializer<DayOfWeek> {
    override val descriptor = PrimitiveSerialDescriptor("DayOfWeek", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): DayOfWeek {
        val text = decoder.decodeString()

        return text.uppercase().let { DayOfWeek.valueOf(it) }
    }

    override fun serialize(encoder: Encoder, value: DayOfWeek) {
        when (value) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
        }.apply { encoder.encodeString(this) }
    }
}