package dk.rohdef.mithfclient

import kotlinx.datetime.TimeZone

class Urls(
    val mithf: String,
    private val timeZone: TimeZone,
) {
    val mitHfAPI = "$mithf/mithf-api.php"
    val mithfLogin = "$mithf/mithflogin.php"
    val shiftApi = "$mithf/vagt-api.php"
}