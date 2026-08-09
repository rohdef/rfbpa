package dk.rohdef.temponizerclient.configuration

import kotlinx.datetime.TimeZone

class TemponizerConfiguration(
    val temponizer: String,
) {
    val timeZone = TimeZone.of("Europe/Copenhagen")
}