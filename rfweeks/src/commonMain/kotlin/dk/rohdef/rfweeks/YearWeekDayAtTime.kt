package dk.rohdef.rfweeks

import arrow.core.Either
import arrow.core.raise.either
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = YearWeekDayAtTime.Serializer::class)
data class YearWeekDayAtTime(
    val yearWeekDay: YearWeekDay,
    val time: LocalTime,
) {
    val yearWeek = yearWeekDay.yearWeek
    val year = yearWeekDay.year
    val week = yearWeekDay.week
    val dayOfWeek = yearWeekDay.dayOfWeek

    val date = yearWeekDay.date

    val localDateTime = date.atTime(time)

    override fun toString(): String {
        return "$year-W${week.toString().padStart(2, '0')}-${dayOfWeek.value}T$time"
    }

    companion object {
        fun parseUnsafe(text: String): YearWeekDayAtTime {
            val parsed = parse(text)
            return when (parsed) {
                is Either.Right -> parsed.value
                is Either.Left -> TODO()
            }
        }

        fun parse(text: String): Either<Unit, YearWeekDayAtTime> = either {
            // TODO: 15/07/2024 rohdef - primitive parsing, assumes dashes exclusively
            // TODO: 15/07/2024 rohdef - primitive parsing, assumes no errors!

            val yearWeekDay = text.substring(0, 10).let { YearWeekDay.parse(it) }.mapLeft { }.bind()
            val time = text.substring(11)
                .let { LocalTime.parse(it) }

            YearWeekDayAtTime(yearWeekDay, time)
        }

        fun from(dateTime: LocalDateTime): YearWeekDayAtTime {
            return YearWeekDayAtTime(
                YearWeekDay.from(dateTime.date),
                dateTime.time,
            )
        }
    }

    object Serializer : KSerializer<YearWeekDayAtTime> {
        override val descriptor = PrimitiveSerialDescriptor("YearWeekDayAtTime", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): YearWeekDayAtTime {
            TODO("not implemented")
        }

        override fun serialize(encoder: Encoder, value: YearWeekDayAtTime) {
            encoder.encodeString("not implemented")
        }
    }
}
