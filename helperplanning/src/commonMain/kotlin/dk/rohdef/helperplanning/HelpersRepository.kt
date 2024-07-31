package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId

interface HelpersRepository {
    suspend fun byId(helperId: HelperId): Either<Unit, Helper>

    suspend fun byShortName(name: String): Either<Unit, Helper>

    suspend fun create(helperId: HelperId): Either<Unit, Helper>
}
