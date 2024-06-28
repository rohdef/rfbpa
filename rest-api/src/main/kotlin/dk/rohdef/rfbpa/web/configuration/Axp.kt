package dk.rohdef.rfbpa.configuration

import kotlinx.serialization.Serializable

data class Axp(
    val host: String,
    val username: String,
    val password: String,
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Axp {
            return Axp(
                map["host"] as String,
                map["username"] as String,
                map["password"] as String
            )
        }
    }
}
