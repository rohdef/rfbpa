package dk.rohdef.rfbpa.web.persistance.helpers

import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId

// TODO: 28/10/2024 rohdef - probably bad test helper - look further into
internal object TestHelpers {
    val fiktivus = Helper(
        HelperId.generateId(),
        "Fiktivus Maximus",
        "fiktivus",
    )

    val realis = Helper(
        HelperId.generateId(),
        "Realis Minimalis",
        "realis",
    )
}
