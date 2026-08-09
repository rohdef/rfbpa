package dk.rohdef.mithfclient

import dk.rohdef.MithfSalarySystem
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.mithfclient.authentication.Credentials
import dk.rohdef.mithfclient.configuration.MithfConfiguration
import dk.rohdef.temponizerclient.configuration.TemponizerConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

fun main() {
    val log = KotlinLogging.logger { }

    val mithfConfiguration = MithfConfiguration(
        "https://mithf.handicapformidlingen.dk",
    )

    val temponizerConfiguration = TemponizerConfiguration(
        "https://handicapformidlingen.temponizer.dk",
    )

    val mithfSalarySystem = MithfSalarySystem(
        mithfConfiguration,
        temponizerConfiguration,
    )

    mithfSalarySystem.registerCredentials(
        RfbpaPrincipal.Subject("dummy"),
        Credentials(
            "200093",
            "w#Ge2yFp7AmtM^VfBrHA",
        ),
    )

    runBlocking {
        val hs = mithfSalarySystem.helpers(RfbpaPrincipal.Subject("dummy"))
        log.info { hs }
    }
}