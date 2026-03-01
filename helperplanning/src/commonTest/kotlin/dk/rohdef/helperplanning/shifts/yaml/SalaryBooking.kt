package dk.rohdef.helperplanning.shifts.yaml

import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.salary_shifts.SalaryBooking as Sb
import dk.rohdef.helperplanning.shifts.HelperBooking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SalaryBooking.Serializer::class)
sealed interface SalaryBooking {
    private typealias HelperResolver = suspend (String)-> HelperId

    @Serializable
    data class Helper(val helper: String) : SalaryBooking {
        override suspend fun asSalaryBooking(resolver: HelperResolver): Sb {
            return Sb.Helper(resolver(helper))
        }

        override suspend fun asExpectedHelperBooking(byHelper: HelperResolver, byShift: HelperId): HelperBooking {
            return HelperBooking.Booked(byHelper(helper))
        }

        override fun helperShortName(): String = helper
    }

    @Serializable
    data class Unknown(val helper: String) : SalaryBooking {
        override suspend fun asSalaryBooking(resolver: HelperResolver): Sb {
            return Sb.UnknownHelper(resolver(helper))
        }

        override suspend fun asExpectedHelperBooking(byHelper: HelperResolver, byShift: HelperId): HelperBooking {
            return HelperBooking.Booked(byHelper(helper))
        }

        override fun helperShortName(): String = helper
    }

    @Serializable
    object Vacancy : SalaryBooking  {
        override suspend fun asSalaryBooking(resolver: HelperResolver) = Sb.Vacancy

        override suspend fun asExpectedHelperBooking(byHelper: HelperResolver, byShift: HelperId): HelperBooking {
            return HelperBooking.Booked(byShift)
        }

        override fun helperShortName() = null
    }

    @Serializable
    object NotBooked : SalaryBooking {
        override suspend fun asSalaryBooking(resolver: HelperResolver) =  Sb.NoBooking

        override suspend fun asExpectedHelperBooking(byHelper: HelperResolver, byShift: HelperId) = HelperBooking.NoBooking

        override fun helperShortName() = null
    }

    suspend fun asSalaryBooking(resolver: HelperResolver): Sb
    suspend fun asExpectedHelperBooking(byHelper: HelperResolver, byShift: HelperId): HelperBooking
    fun helperShortName(): String?

    object Serializer : KSerializer<SalaryBooking> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("SalaryBooking", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): SalaryBooking {
            val text = decoder.decodeString()
                .lowercase()
                .trim()

            val helper = "helper"
            val vacancy = "unknown"
            return when {
                text.startsWith(helper) -> Helper(extractHelper(text.substring(helper.length)))
                text.startsWith(vacancy) -> Unknown(extractHelper(text.substring(helper.length)))
                text.equals("vacancy") -> Vacancy
                text.equals("not booked") -> NotBooked
                else -> throw IllegalArgumentException("Unknown salary booking type: $text")
            }
        }

        private fun extractHelper(text: String): String {
            val regex = """\(([a-zA-Z ]+)\)""".toRegex()
            return regex.find(text)
                ?.groupValues
                ?.get(1)
                ?: throw IllegalArgumentException("Invalid helper format in: $text")
        }

        override fun serialize(encoder: Encoder, value: SalaryBooking) {
            val result = when (value) {
                is Helper -> "helper(${value.helper})"
                is Unknown -> "unknown(${value.helper})"
                Vacancy -> "vacancy"
                NotBooked -> "not booked"
            }

            encoder.encodeString(result)
        }
    }
}