package dk.rohdef.rfbpa.configuration

data class Mithf(
    val mithfHost: String,
    val temponizerHost: String,
    val username: String,
    val password: String,
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Mithf {
            return Mithf(
                map["mithfHost"] as String,
                map["temponizerHost"] as String,
                map["username"] as String,
                map["password"] as String,
            )
        }
    }
}
