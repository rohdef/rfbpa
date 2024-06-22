package dk.rohdef.rfbpa.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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
    get("/calendar") {
        val principal = call.principal<JWTPrincipal>()
        val username = principal!!.payload.subject
        val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())

        log.info { "Reading calendar details" }
        log.info { "${call.request.queryParameters["key"]}" }
        call.request.headers.forEach { name, values ->
            log.info { "${name}: ${values.joinToString(", ")}" }
        }
        val calendar = Calendar()
            .withProdId("-//Rohde Fischer//RF-BPA//DA")
            .withDefaults()
            .fluentTarget

        val uidGenerator = RandomUidGenerator()

        val eventTimes = listOf(
            LocalDateTime.parse("2024-06-11T11:46:00") to LocalDateTime.parse("2024-06-11T13:46:00"),
            LocalDateTime.parse("2024-06-15T11:46:00") to LocalDateTime.parse("2024-06-15T11:46:00"),
            LocalDateTime.parse("2024-06-17T06:46:00") to LocalDateTime.parse("2024-06-17T17:46:00"),
            LocalDateTime.parse("2024-06-19T11:46:00") to LocalDateTime.parse("2024-06-23T11:46:00"),
        )

        val updCAl = eventTimes.map {
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

        call.respondText(updCAl.toString())
    }
}