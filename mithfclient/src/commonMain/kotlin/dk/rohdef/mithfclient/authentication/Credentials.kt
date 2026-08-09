package dk.rohdef.mithfclient.authentication

data class Credentials(
    val username: String,
    val password: String,
) {
    override fun toString(): String = "Credentials(username=$username, password=***)"
}