@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.rfbpa.web

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.RfbpaPrincipal
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.MithfSalarySystem
import dk.rohdef.mithfclient.authentication.Credentials
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.uuid.ExperimentalUuidApi

class Seeder(
    private val helperRepository: HelpersRepository,
    private val config: RfBpaConfig,
    private val mithf: MithfSalarySystem,
) {
    private val log = KotlinLogging.logger { }

    suspend fun users() {
        mithf.registerCredentials(
            RfbpaPrincipal.Subject("Dummy"),
            Credentials(
                config.mithf.username,
                config.mithf.password,
            ),
        )
    }

    suspend fun seedHelpers(): Either<SeedError, List<Helper>> = either {
        log.info { "Seeding helpers" }
        // TODO: 01/08/2024 rohdef - find better way to inject test data
        val salaryHelpers = mithf.helpers(
            RfbpaPrincipal.Subject("Dummy")
        )
            .mapLeft { TODO() }
            .bind()

        salaryHelpers
            .map { it.toHelper() }
            .map { helper ->
                log.info { "Seeding helper ${helper.name}" }
                helperRepository.create(helper)
                    .mapLeft { SeedError.CannotCreateHelper(helper) }
                    .bind()
            }
    }

    sealed interface SeedError {
        data class CannotCreateHelper(
            val helper: Helper,
        ) : SeedError
    }
}