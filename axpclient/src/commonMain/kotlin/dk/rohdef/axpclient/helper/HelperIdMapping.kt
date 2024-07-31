package dk.rohdef.axpclient.helper

import dk.rohdef.helperplanning.helpers.HelperId

data class HelperIdMapping(
    val helperId: HelperId,
    val axpTid: HelperTID,
    val axpNumber: HelperNumber,
)
