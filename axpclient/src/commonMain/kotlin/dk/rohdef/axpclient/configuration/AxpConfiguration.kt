package dk.rohdef.axpclient.configuration

import kotlinx.datetime.TimeZone

data class AxpConfiguration(
    val url: String,
    val username: String,
    val password: String,
) {
    val timeZone = TimeZone.of("Europe/Copenhagen")
}
