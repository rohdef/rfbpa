package dk.rohdef.rfbpa.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.util.RandomUidGenerator

private val log = KotlinLogging.logger {}
fun Route.calendar() {
    authenticate("calendar") {
        get("/calendar") {
            val calendar = Calendar()
                .withProdId("-//Rohde Fischer//RF-BPA//DA")
                .withDefaults()
                .fluentTarget

            val uidGenerator = RandomUidGenerator()

            val start = LocalDateTime.parse("2024-06-15T11:46:00")
            val end = LocalDateTime.parse("2024-06-16T08:11:00")

            val event = VEvent(
                start.toJavaLocalDateTime(),
                end.toJavaLocalDateTime(),
                "Arbejde"
            )
                .withProperty(uidGenerator.generateUid())
                .getFluentTarget<VEvent>()

            log.info { "${event.uid.map { it.value }.orElse("no")}" }

            val updCAl = calendar.withComponent(event).fluentTarget

            call.respondText(updCAl.toString())
        }
    }
}
