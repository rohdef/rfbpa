package dk.rohdef.rfbpa.web.errors

import arrow.core.NonEmptyList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ErrorData {
    @Serializable
    @SerialName("NoData")
    object NoData: ErrorData

    @Serializable
    @SerialName("ErrorList")
    data class MultipleErrors(val errors: List<ErrorData>) : ErrorData {
        constructor(errors: NonEmptyList<ErrorData>) : this(errors.toList())
        constructor(error: ErrorData, vararg errors: ErrorData) : this(listOf(error) + errors.toList())

        init {
            if (errors.isEmpty()) { throw IllegalArgumentException("No errors were provided") }
        }
    }

    @Serializable
    @SerialName("FormatError")
    data class FormatError(
        val input: String,
        val expected: String,
    ) : ErrorData
}

