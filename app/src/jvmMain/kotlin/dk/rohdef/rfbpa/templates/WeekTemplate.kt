package dk.rohdef.rfbpa.templates

import arrow.core.*
import dk.rohdef.rfweeks.YearWeek
import kotlinx.datetime.DateTimePeriod
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
    val start: YearWeek,
    @Serializable(OptionalTimePeriodSerializer::class)
    val repeat: Option<DateTimePeriod> = none(),
    val shifts: Map<@Serializable(DayOfWeekSerializer::class) DayOfWeek, List<ShiftTemplate>>
) {
    class OptionalTimePeriodSerializer : KSerializer<Option<DateTimePeriod>> {
        private val dateTimePeriodSerializer = DateTimePeriod.serializer()
        override val descriptor = PrimitiveSerialDescriptor("OptionalYearWeek", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Option<DateTimePeriod> {
            val text = decoder.decodeString()

            return when (text) {
                "none" -> none()
                else -> DateTimePeriod.parse(text).some()
            }
        }

        override fun serialize(encoder: Encoder, value: Option<DateTimePeriod>) {
            when (value) {
                is Some -> dateTimePeriodSerializer.serialize(encoder, value.value)
                None -> encoder.encodeString("none")
            }
        }
    }

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
