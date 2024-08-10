package dk.rohdef.rfbpa.web.templates

import arrow.core.getOrElse
import dk.rohdef.helperplanning.templates.Template
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.rfweeks.YearWeekInterval
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.mamoe.yamlkt.Yaml
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}
fun Route.templates() {
    val templateApplier: TemplateApplier by inject()

    post("/templates/{yearWeekInterval}") {
        val yearWeekInterval = call.parameters["yearWeekInterval"]!!
            .let { YearWeekInterval.parse(it) }
            .getOrElse { TODO("No handling of invalid intervals yet") }

        val multipartItem = call.receiveMultipart().readPart()!!
        val bytes = when (multipartItem) {
            is PartData.BinaryChannelItem -> TODO()
            is PartData.BinaryItem -> TODO()
            is PartData.FileItem -> multipartItem.streamProvider().readBytes()
            is PartData.FormItem -> TODO()
        }
        val rawTemplate = bytes.toString(Charsets.UTF_8)
        val template = Yaml.decodeFromString(Template.serializer(), rawTemplate)

        templateApplier.applyTemplates(yearWeekInterval, listOf(template))

        call.respond("Nothing to see here")
    }
}
