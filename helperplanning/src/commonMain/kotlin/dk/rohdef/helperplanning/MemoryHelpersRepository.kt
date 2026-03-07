package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.getOrNone
import arrow.core.right
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersError
import dk.rohdef.helperplanning.helpers.HelpersRepository

class MemoryHelpersRepository : HelpersRepository {
    private val _helpers = mutableMapOf<HelperId, Helper>()

    fun reset() {
        _helpers.clear()
    }

    override suspend fun all(): List<Helper> = _helpers.values.toList()

    override suspend fun byId(helperId: HelperId): Either<HelpersError.CannotFindHelperById, Helper> {
        return _helpers.getOrNone(helperId)
            .toEither { HelpersError.CannotFindHelperById(helperId) }
    }

    override suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper> {
        return _helpers
            .values
            .firstOrNone { it.shortName == shortName }
            .toEither { HelpersError.CannotFindHelperByShortName(shortName) }
    }

    override suspend fun create(helper: Helper): Either<HelpersError.Create, Helper> {
        _helpers[helper.id] = helper
        return helper.right()
    }
}