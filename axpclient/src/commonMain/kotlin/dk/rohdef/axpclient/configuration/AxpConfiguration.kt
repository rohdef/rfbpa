package dk.rohdef.axpclient.configuration

import kotlinx.datetime.TimeZone

data class AxpConfiguration(
    val timeZone: TimeZone,
    val url: String,
    val username: String,
    val password: String,
)
