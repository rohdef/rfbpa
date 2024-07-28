package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.configuration.Axp
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.configuration.RuntimeMode
import dk.rohdef.rfbpa.web.configuration.Auth
import java.net.URL

object TestConfiguration {
    val default = RfBpaConfig(
        Axp("", "", ""),
        Auth(
            URL("http://localhost:1234/test"),
            "http://localhost:1234/test"
        ),
        RuntimeMode.TEST,
    )
}
