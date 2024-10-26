package dk.rohdef.helperplanning.helpers

import arrow.core.Either

interface HelpersRepository {
    suspend fun all(): List<Helper>
    suspend fun byId(helperId: HelperId): Either<HelpersError.CannotFindHelperById, Helper>
    suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper.Permanent>

    suspend fun create(helper: Helper): Either<HelpersError.Create, Helper>
}
