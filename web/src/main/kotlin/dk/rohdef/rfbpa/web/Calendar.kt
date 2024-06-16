package dk.rohdef.rfbpa.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.FluentCalendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.util.RandomUidGenerator

private val log = KotlinLogging.logger {}
fun Route.calendar() {
//    authenticate("calendar") {
        get("/calendar") {
            log.info { "Reading calendar details" }
            val calendar = Calendar()
                .withProdId("-//Rohde Fischer//RF-BPA//DA")
                .withDefaults()
                .fluentTarget

            val uidGenerator = RandomUidGenerator()

            val x = listOf(
                LocalDateTime.parse("2024-06-11T11:46:00") to LocalDateTime.parse("2024-06-11T13:46:00"),
                LocalDateTime.parse("2024-06-15T11:46:00") to LocalDateTime.parse("2024-06-15T11:46:00"),
                LocalDateTime.parse("2024-06-17T06:46:00") to LocalDateTime.parse("2024-06-17T17:46:00"),
                LocalDateTime.parse("2024-06-19T11:46:00") to LocalDateTime.parse("2024-06-23T11:46:00"),
            )

            val updCAl = x.map {
                VEvent(
                    it.first.toJavaLocalDateTime(),
                    it.second.toJavaLocalDateTime(),
                    "Arbejde hos Rohde"
                )
                    .withProperty(uidGenerator.generateUid())
                    .getFluentTarget<VEvent>()
            }
                .fold(calendar) { calendar: FluentCalendar, event ->
                    calendar.withComponent(event)
                }
                .fluentTarget

            val start = LocalDateTime.parse("2024-06-15T11:46:00")
            val end = LocalDateTime.parse("2024-06-16T08:11:00")

            val event = VEvent(
                start.toJavaLocalDateTime(),
                end.toJavaLocalDateTime(),
                "Arbejde"
            )
                .withProperty(uidGenerator.generateUid())
                .getFluentTarget<VEvent>()

            call.respondText(updCAl.toString())
        }
//    }
}
