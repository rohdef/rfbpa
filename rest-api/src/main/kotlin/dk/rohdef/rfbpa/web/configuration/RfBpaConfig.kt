package dk.rohdef.rfbpa.configuration

import dk.rohdef.rfbpa.web.configuration.Auth

data class RfBpaConfig(
    val axp: Axp,
    val auth: Auth,
    val runtimeMode: RuntimeMode,
) {
    companion object {
        fun fromMap(map: Map<String, Any>): RfBpaConfig {
            val env = map.get("environment") as String
            val axp = map.get("axp") as Map<String, Any>
            val auth = map.get("auth") as Map<String, String>

            val runtimeMode = RuntimeMode.valueOf(env.uppercase())

            return RfBpaConfig(
                Axp.fromMap(axp),
                Auth.fromMap(auth),
                runtimeMode,
            )
        }
    }
}
