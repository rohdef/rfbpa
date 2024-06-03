package dk.rohdef.helperplanning.templates

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(HelperReservationSerializer::class)
sealed interface HelperReservation {
    object NoReservation : HelperReservation

    data class Helper(
        val id: String,
    ) : HelperReservation
}

class HelperReservationSerializer : KSerializer<HelperReservation> {
    override val descriptor = PrimitiveSerialDescriptor("HelperReservation", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): HelperReservation {
        val text = decoder.decodeString()

        return when (text) {
            "none" -> HelperReservation.NoReservation
            else -> HelperReservation.Helper(text)
        }
    }

    override fun serialize(encoder: Encoder, value: HelperReservation) {
        when (value) {
            is HelperReservation.Helper -> encoder.encodeString(value.id)
            HelperReservation.NoReservation -> encoder.encodeString("none")
        }
    }
}
