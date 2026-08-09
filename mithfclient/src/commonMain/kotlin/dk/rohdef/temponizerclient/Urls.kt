package dk.rohdef.temponizerclient

import kotlinx.datetime.TimeZone

class Urls(
    val temponizer: String,
    private val timeZone: TimeZone,
) {
    val temponizerIndex = "$temponizer/index.php"
    val temponizerLogin = "$temponizerIndex?page=loginSSOClient"
    val helpers = "$temponizerIndex?page=worker_pools"
}