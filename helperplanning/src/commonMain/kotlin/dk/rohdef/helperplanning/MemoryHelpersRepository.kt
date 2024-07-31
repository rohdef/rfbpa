package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.right
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId

class MemoryHelpersRepository : HelpersRepository {
    private val _helpers = mutableListOf<Helper>()

    override suspend fun all(): List<Helper> = _helpers.toList()

    override suspend fun byId(helperId: HelperId): Either<Unit, Helper> {
        return _helpers.firstOrNone() { it.id == helperId }.toEither { }
    }

    override suspend fun byShortName(shortName: String): Either<Unit, Helper> {
        return _helpers.firstOrNone { it.shortName == shortName }.toEither { }
    }

    override suspend fun create(helper: Helper): Either<Unit, Helper> {
        _helpers.add(helper)
        return helper.right()
    }
}
