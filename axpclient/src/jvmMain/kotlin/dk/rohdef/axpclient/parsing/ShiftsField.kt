package dk.rohdef.axpclient.parsing

internal enum class ShiftsField(val index: Int) {
    Day(1),
    Evening(2),
    Night(3),
    AllDay(4),
    Long(5),
    Illness(7),
}