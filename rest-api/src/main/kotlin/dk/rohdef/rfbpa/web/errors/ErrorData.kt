package dk.rohdef.rfbpa.web.errors

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
        constructor(vararg errors: ErrorData) : this(errors.toList())
    }

    @Serializable
    @SerialName("FormatError")
    data class FormatError(
        val input: String,
        val expected: String,
    ) : ErrorData
}

