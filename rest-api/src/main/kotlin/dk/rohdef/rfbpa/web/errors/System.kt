package dk.rohdef.rfbpa.web.errors

sealed interface System : ErrorType {
    object Unknown : System
}