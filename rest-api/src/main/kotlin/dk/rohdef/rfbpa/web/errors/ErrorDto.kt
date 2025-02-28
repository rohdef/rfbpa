package dk.rohdef.rfbpa.web.errors

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
    @Serializable(with = ErrorTypeSerializer::class)
    val type: ErrorType,
    val supplementary: ErrorData,
    val message: String,
)

@Serializable
sealed interface ErrorData

@Serializable
@SerialName("NoData")
object NoData: ErrorData
