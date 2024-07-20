package dk.rohdef.axpclient.shift

internal data class Weekday(
    val day: List<AxpShift>,
    val evening: List<AxpShift>,
    val night: List<AxpShift>,
    val all24Hours: List<AxpShift>,
    val long: List<AxpShift>,

    val illness: List<AxpShift>,
) {
    val allShifts =
        day + evening + night + all24Hours + long
}
