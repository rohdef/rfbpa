package dk.rohdef.axpclient.helper

import dk.rohdef.helperplanning.helpers.HelperId

// TODO make internal once database wrapper is there
data class HelperIdMapping(
    val helperID: HelperId,
    val axpTid: HelperTID,
    val axpNumber: HelperNumber,
)
