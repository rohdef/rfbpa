package dk.rohdef.axpclient

internal sealed interface LoginResult {
    object NotAuthorized : LoginResult

    data class InvalidResult(val description: String) : LoginResult

    object Success : LoginResult
}