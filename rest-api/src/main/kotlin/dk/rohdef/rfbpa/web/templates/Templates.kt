package dk.rohdef.rfbpa.web.templates

import arrow.core.raise.either
import dk.rohdef.helperplanning.templates.Template
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.rfbpa.web.modules.rfbpaPrincipal
import dk.rohdef.rfbpa.web.parseYearWeekInterval
import dk.rohdef.rfbpa.web.typedPost
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import net.mamoe.yamlkt.Yaml
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}
fun Route.templates() {
    val templateApplier: TemplateApplier by inject()
    typedPost("/templates/{yearWeekInterval}") {
        either {
            log.info { "Applying shift template for interval: ${call.parameters["yearWeekInterval"]}" }
            val principal = call.rfbpaPrincipal().bind()
            val yearWeekInterval = parseYearWeekInterval(call.parameters["yearWeekInterval"])

            val multipartItem = call.receiveMultipart().readPart()!!
            val bytes = when (multipartItem) {
                is PartData.BinaryChannelItem -> TODO()
                is PartData.BinaryItem -> TODO()
                is PartData.FileItem -> multipartItem.provider().toByteArray()
                is PartData.FormItem -> TODO()
            }
            val rawTemplate = bytes.toString(Charsets.UTF_8)
            val template = Yaml.decodeFromString(Template.serializer(), rawTemplate)

            templateApplier.applyTemplates(
                principal,
                yearWeekInterval,
                listOf(template),
            )

            log.info { "Ready for action\n\n$principal\n\n$yearWeekInterval" }
            "Nothing to see here"
        }
    }
}
