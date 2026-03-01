package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.rfbpa.web.persistance.axp.HelperDataBaseItem
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml
import java.nio.file.Paths
import kotlin.io.path.readText

class Seeder(
    private val helperRepository: HelpersRepository,
    private val helperReferenceRepository: AxpHelperReferences,
) {
    private val log = KotlinLogging.logger { }

    suspend fun seedHelpers(): Either<SeedError, List<Helper>> = either {
        log.info { "Seeding helpers" }
        // TODO: 01/08/2024 rohdef - find better way to inject test data
        val helpers = Paths.get("helpers.yaml").readText()
            .let { Yaml.decodeFromString<Map<String, HelperDataBaseItem>>(it) }

        helpers
            .mapKeys { Helper.Permanent(it.key, it.key, HelperId.generateId()) }
            .mapKeys { helper -> helperRepository.create(helper.key).mapLeft { SeedError.CannotCreateHelper(helper.key) }.bind() }
            .map { helper ->
                val tid = HelperTID(helper.value.helperTid)
                val number = HelperNumber(helper.value.helperNumber)
                val helperId = helper.key.id
                helperReferenceRepository.createHelperReference(number, tid, helperId)
                    .mapLeft {
                        SeedError.CannotCreateReference(
                            helper.value.helperTid,
                            helper.value.helperNumber,
                        )
                    }
                    .bind()
                helper.key
        }
    }

    sealed interface SeedError {
        data class CannotCreateReference(
            val tid: String,
            val helperNumber: String,
        ) : SeedError

        data class CannotCreateHelper(
            val helper: Helper,
        ) : SeedError
    }
}