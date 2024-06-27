package dk.rohdef.rfbpa.configuration

import kotlinx.serialization.Serializable

@Serializable
data class RfBpaConfig(
    val axp: Axp,
    val runtimeMode: RuntimeMode,
) {
    companion object {
        fun fromMap(map: Map<String, Any>): RfBpaConfig {
            val env = map.get("environment") as String
            val axp = map.get("axp") as Map<String, Any>

            val runtimeMode = RuntimeMode.valueOf(env.uppercase())

            return RfBpaConfig(
                Axp.fromMap(axp),
                runtimeMode,
            )
        }
    }
}
