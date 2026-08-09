package dk.rohdef.mithfclient.authentication

@JvmInline
value class Btok(
    val token: String,
) {
    override fun toString() = "Btok(token=***)"
}
