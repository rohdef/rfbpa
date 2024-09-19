package dk.rohdef.rfbpa.web.templates

import arrow.core.getOrElse
import arrow.core.raise.either
import dk.rohdef.helperplanning.templates.Template
import dk.rohdef.helperplanning.templates.TemplateApplier
import dk.rohdef.rfbpa.web.ApiError
import dk.rohdef.rfbpa.web.modules.rfbpaPrincipal
import dk.rohdef.rfbpa.web.typedPost
import dk.rohdef.rfweeks.YearWeekInterval
import dk.rohdef.rfweeks.YearWeekIntervalParseError
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

    typedPost("/templates/{yearWeekInterval}") {
        either {
            log.info { "Apploying shift template for interval: ${call.parameters["yearWeekInterval"]}" }
            val principal = call.rfbpaPrincipal().bind()

            val yearWeekInterval = call.parameters["yearWeekInterval"]!!
                .let { YearWeekInterval.parse(it) }
                .mapLeft { it.first() }
                .mapLeft {
                    when (it) {
                        is YearWeekIntervalParseError.NoSeparatorError ->
                            ApiError.badRequest("Could not find interval separator, please use double hyphen '--'")

                        is YearWeekIntervalParseError.YearWeekComponentParseError ->
                            ApiError.badRequest("Parsing of year weeks failed")
                    }
                }
                .bind()

            val multipartItem = call.receiveMultipart().readPart()!!
            val bytes = when (multipartItem) {
                is PartData.BinaryChannelItem -> TODO()
                is PartData.BinaryItem -> TODO()
                is PartData.FileItem -> multipartItem.streamProvider().readBytes()
                is PartData.FormItem -> TODO()
            }
            val rawTemplate = bytes.toString(Charsets.UTF_8)
            val template = Yaml.decodeFromString(Template.serializer(), rawTemplate)

            templateApplier.applyTemplates(
                principal,
                yearWeekInterval,
                listOf(template),
            )

            "Nothing to see here"
        }
    }
}
