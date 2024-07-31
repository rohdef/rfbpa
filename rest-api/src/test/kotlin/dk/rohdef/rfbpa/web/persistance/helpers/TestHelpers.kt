package dk.rohdef.rfbpa.web.persistance.helpers

import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId

internal object TestHelpers {
    val fiktivus = Helper(
        HelperId.generateId(),
        "fiktivus",
    )

    val realis = Helper(
        HelperId.generateId(),
        "realis",
    )
}
