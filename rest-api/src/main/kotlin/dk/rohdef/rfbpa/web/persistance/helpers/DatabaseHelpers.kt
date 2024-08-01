package dk.rohdef.rfbpa.web.persistance.helpers

import arrow.core.Either
import arrow.core.right
import dk.rohdef.helperplanning.HelpersRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.ResultRow

class DatabaseHelpers : HelpersRepository {
    private fun rowToHelper(row: ResultRow): Helper {
        return Helper(
            HelperId(row[HelpersTable.id].toKotlinUUID()),
            row[HelpersTable.shortName]
        )
    }

    override suspend fun all(): List<Helper> {
        return listOf()
    }

    override suspend fun byId(helperId: HelperId): Either<Unit, Helper> {
        TODO("not implemented")
    }

    override suspend fun byShortName(shortName: String): Either<Unit, Helper> {
        TODO("not implemented")
    }

    override suspend fun create(helper: Helper): Either<Unit, Helper> {
        return helper.right()
    }
}
