package dk.rohdef.temponizerclient.authentication

data class TemponizerCredentials(
    val userId: String,
    val t: String,
    val token: String,
)