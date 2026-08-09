package dk.rohdef.mithfclient

import com.fleeksoft.ksoup.Ksoup
import dk.rohdef.temponizerclient.authentication.TemponizerCredentials

class MithfParser {
    fun parseSsoCredentials(body: String): TemponizerCredentials {
        val ssoForm = Ksoup.parse(body)
            .body()
            .select("#sso")
            .get(0)

        val userId = ssoForm
            .select("input[name=brugere_id]")
            .attr("value")
        val t = ssoForm
            .select("input[name=t]")
            .attr("value")
        val token = ssoForm
            .select("input[name=token]")
            .attr("value")

        return TemponizerCredentials(
            userId,
            t,
            token,
        )
    }
}