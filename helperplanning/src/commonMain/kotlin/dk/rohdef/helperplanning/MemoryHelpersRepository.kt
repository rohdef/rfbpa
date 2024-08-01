package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.right
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersError

class MemoryHelpersRepository : HelpersRepository {
    private val _helpers = mutableListOf<Helper>()

    fun reset() {
        _helpers.clear()
    }

    override suspend fun all(): List<Helper> = _helpers.toList()

    override suspend fun byId(helperId: HelperId): Either<HelpersError.CannotFindHelperById, Helper> {
        return _helpers.firstOrNone() { it.id == helperId }
            .toEither { HelpersError.CannotFindHelperById(helperId) }
    }

    override suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper> {
        return _helpers.firstOrNone { it.shortName == shortName }
            .toEither { HelpersError.CannotFindHelperByShortName(shortName) }
    }

    override suspend fun create(helper: Helper): Either<HelpersError.Create, Helper> {
        _helpers.add(helper)
        return helper.right()
    }
}
