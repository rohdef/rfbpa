package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersError

interface HelpersRepository {
    suspend fun all(): List<Helper>
    suspend fun byId(helperId: HelperId): Either<HelpersError.CannotFindHelperById, Helper>
    suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper>

    suspend fun create(helper: Helper): Either<HelpersError.Create, Helper>
}
