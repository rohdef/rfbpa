package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId

interface HelpersRepository {
    suspend fun all(): List<Helper>
    suspend fun byId(helperId: HelperId): Either<Unit, Helper>
    suspend fun byShortName(shortName: String): Either<Unit, Helper>

    suspend fun create(helper: Helper): Either<Unit, Helper>
}
