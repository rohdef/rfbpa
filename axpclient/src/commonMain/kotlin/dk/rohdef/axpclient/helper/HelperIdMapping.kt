package dk.rohdef.axpclient.helper

import dk.rohdef.helperplanning.helpers.Helper

// TODO make internal once database wrapper is there
data class HelperIdMapping(
    val helperID: Helper.ID,
    val axpTid: HelperTID,
    val axpNumber: HelperNumber,
)
