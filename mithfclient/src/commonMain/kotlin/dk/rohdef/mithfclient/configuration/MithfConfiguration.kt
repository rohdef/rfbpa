package dk.rohdef.mithfclient.configuration

import kotlinx.datetime.TimeZone

class MithfConfiguration(
    val mithf: String,
) {
    val timeZone = TimeZone.of("Europe/Copenhagen")
}