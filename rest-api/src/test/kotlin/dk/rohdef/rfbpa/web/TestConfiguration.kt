package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.configuration.Mithf
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.configuration.Auth
import java.net.URI

object TestConfiguration {
    val default = RfBpaConfig(
        Mithf("", "", "", ""),
        Auth(
            URI("http://localhost:1234/realms/rfbpa/protocol/openid-connect/certs").toURL(),
            "http://localhost:1234/test"
        ),
        RuntimeMode.TEST,
    )
}
