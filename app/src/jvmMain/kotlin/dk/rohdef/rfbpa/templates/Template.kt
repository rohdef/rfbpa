package dk.rohdef.rfbpa.templates

import arrow.core.*
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeek.Companion.parse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Template(
    val start: YearWeek,
    // TODO: 02/06/2024 rohdef - should probably be sealed class instead
    @Serializable(OptionalYearWeekSerializer::class)
    val end: Option<YearWeek>,
//    val end: String,
    // TODO: 27/05/2024 rohdef - validate that the weeks will never overlap
    val weeks: List<WeekTemplate>,
) {
    class OptionalYearWeekSerializer : KSerializer<Option<YearWeek>> {
        private val yearWeekKSerializer = YearWeek.serializer()
        override val descriptor = PrimitiveSerialDescriptor("OptionalYearWeek", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Option<YearWeek> {
            val text = decoder.decodeString()

            return when (text) {
                "none" -> none()
                else -> {
                    val yearWeek = parse(text)

                    when (yearWeek) {
                        is Either.Right -> yearWeek.value.some()
                        is Either.Left -> throw IllegalArgumentException("Could not deserialize year and week: ${yearWeek.value}")
                    }
                }
            }
        }

        override fun serialize(encoder: Encoder, value: Option<YearWeek>) {
            when (value) {
                is Some -> yearWeekKSerializer.serialize(encoder, value.value)
                None -> encoder.encodeString("none")
            }
        }
    }
}
