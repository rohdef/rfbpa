package dk.rohdef.rfbpa.web.errors

sealed interface Parsing : ErrorType {
    object InvalidYearWeekInterval : Parsing
    object InvalidUUID : Parsing
    object Unknown : Parsing
}