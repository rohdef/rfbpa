package dk.rohdef.helperplanning.shifts.yaml

import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.HelperBooking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = RfbpaBooking.Serializer::class)
sealed interface RfbpaBooking {
    private typealias HelperResolver = suspend (String)-> HelperId

    @Serializable
    data class Helper(val helper: String) : RfbpaBooking {
        override suspend fun asHelperBooking(helperResolver: HelperResolver): HelperBooking {
            return HelperBooking.Booked(helperResolver(helper))
        }
    }

    @Serializable
    object NotBooked : RfbpaBooking {
        override suspend fun asHelperBooking(helperResolver: HelperResolver) = HelperBooking.NoBooking
    }

    suspend fun asHelperBooking(helperResolver: HelperResolver): HelperBooking

    object Serializer : KSerializer<RfbpaBooking> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("SalaryBooking", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): RfbpaBooking {
            val text = decoder.decodeString()
                .lowercase()
                .trim()

            val helper = "helper"
            return when {
                text.startsWith(helper) -> Helper(extractHelper(text.substring(helper.length)))
                text.equals("not booked") -> NotBooked
                else -> throw IllegalArgumentException("Unknown rfbpa booking type: $text")
            }
        }

        private fun extractHelper(text: String): String {
            val regex = """\(([a-zA-Z ]+)\)""".toRegex()
            return regex.find(text)
                ?.groupValues
                ?.get(1)
                ?: throw IllegalArgumentException("Invalid helper format in: $text")
        }

        override fun serialize(encoder: Encoder, value: RfbpaBooking) {
            val result = when (value) {
                is Helper -> "helper(${value.helper})"
                NotBooked -> "not booked"
            }

            encoder.encodeString(result)
        }
    }
}