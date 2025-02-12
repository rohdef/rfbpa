package dk.rohdef.rfbpa.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto (
    val type: ErrorType,
    val message: String,
    val supplementary: ErrorData,
) {
    @Serializable
    sealed interface ErrorType
}

@Serializable
sealed interface ErrorData

@Serializable
@SerialName("NoData")
object NoData : ErrorData

@Serializable
object UnknownErrorType : ErrorDto.ErrorType