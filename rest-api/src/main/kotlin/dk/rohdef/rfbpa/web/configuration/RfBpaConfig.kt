package dk.rohdef.rfbpa.configuration

import dk.rohdef.rfbpa.web.configuration.Auth

data class RfBpaConfig(
    val mithf: Mithf,
    val auth: Auth,
    val runtimeMode: RuntimeMode,
) {
    companion object {
        fun fromMap(map: Map<String, Any>): RfBpaConfig {
            val env = map.get("environment") as String
            val mithf = map.get("mithf") as Map<String, Any>
            val auth = map.get("auth") as Map<String, String>

            val runtimeMode = RuntimeMode.valueOf(env.uppercase())

            return RfBpaConfig(
                Mithf.fromMap(mithf),
                Auth.fromMap(auth),
                runtimeMode,
            )
        }
    }
}
