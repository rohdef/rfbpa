package dk.rohdef.helperplanning

import arrow.core.Either
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId

class MemoryHelpersRepository : HelpersRepository {
    override suspend fun all(): List<Helper> {
        TODO("not implemented")
    }

    override suspend fun byId(helperId: HelperId): Either<Unit, Helper> {
        TODO("not implemented")
    }

    override suspend fun byShortName(name: String): Either<Unit, Helper> {
        TODO("not implemented")
    }

    override suspend fun create(helperId: HelperId): Either<Unit, Helper> {
        TODO("not implemented")
    }
}
