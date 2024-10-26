package dk.rohdef.helperplanning.helpers

import arrow.core.Either

class HelperServiceImplementation(
    private val helperRepository: HelpersRepository,
) : HelperService {
    override suspend fun all(): List<Helper> = helperRepository.all()

    override suspend fun byId(helperId: HelperId): Either<HelpersError.CannotFindHelperById, Helper> =
        helperRepository.byId(helperId)

    override suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper.Permanent> =
        helperRepository.byShortName(shortName)

    override suspend fun create(helper: Helper): Either<HelpersError.Create, Helper> =
        helperRepository.create(helper)
}
