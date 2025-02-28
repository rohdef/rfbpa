package dk.rohdef.rfbpa.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
    val type: ErrorType,
    val supplementary: ErrorData,
    val message: String,
)

@Serializable
sealed interface ErrorData

@Serializable
@SerialName("NoData")
object NoData: ErrorData
