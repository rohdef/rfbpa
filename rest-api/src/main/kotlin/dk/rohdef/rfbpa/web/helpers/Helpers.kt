@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web.helpers

import dk.rohdef.arrowktor.get
import dk.rohdef.arrowktor.httpOk
import dk.rohdef.helperplanning.helpers.HelperService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.resources.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.ExperimentalUuidApi

private val log = KotlinLogging.logger {}
fun Route.helpers() {
    val helperService: HelperService by inject()

    get<Helpers> {
        log.info { "Getting helpers" }
        val principal = principal()
            .bind()
            .domainPrincipal

        val helpers = helperService.all()
        helpers.map { HelperDto(
            it.id.value,
            it.name,
        ) }.httpOk()
    }
}

@Resource("/helpers")
class Helpers {

}