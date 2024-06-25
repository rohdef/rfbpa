package dk.rohdef.rfbpa.web.calendar

import arrow.core.Either
import kotlinx.datetime.Clock

class CalendarService(
    val calendars: CalendarRepository,
    val clock: Clock,
) {
    fun calendarFor(user: String): Either<Any, Any> {
        TODO()
    }

    fun create(): Either<Any, Any> {
        TODO()
    }

    fun modify(): Either<Any, Any> {
        TODO()
    }

    fun delete(): Either<Any, Any> {
        TODO()
    }
}
