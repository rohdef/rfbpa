package dk.rohdef.rfbpa.web.persistance.helpers

import arrow.core.Either
import arrow.core.Either.Companion.catchOrThrow
import arrow.core.firstOrNone
import arrow.core.raise.catch
import arrow.core.right
import dk.rohdef.helperplanning.HelpersRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersError
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.sql.SQLException

private val log = KotlinLogging.logger {}

class DatabaseHelpers : HelpersRepository {
    private fun rowToHelper(row: ResultRow): Helper {
        return Helper(
            HelperId(row[HelpersTable.id].toKotlinUUID()),
            row[HelpersTable.shortName]
        )
    }

    override suspend fun all(): List<Helper> = dbQuery {
        HelpersTable
            .selectAll()
            .map { rowToHelper(it) }
    }

    override suspend fun byId(helperId: HelperId): Either<HelpersError.CannotFindHelperById, Helper> = dbQuery {
        HelpersTable
            .selectAll()
            .where { HelpersTable.id eq helperId.id.toJavaUUID() }
            .map { rowToHelper(it) }
            .firstOrNone()
            .toEither { HelpersError.CannotFindHelperById(helperId) }
    }

    override suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper> =
        dbQuery {
            HelpersTable
                .selectAll()
                .where { HelpersTable.shortName eq shortName }
                .map { rowToHelper(it) }
                .firstOrNone()
                .toEither { HelpersError.CannotFindHelperByShortName(shortName) }
        }

    override suspend fun create(helper: Helper): Either<HelpersError.Create, Helper> = dbQuery {
        catchOrThrow<Exception, Helper> {
            HelpersTable.insert {
                it[id] = helper.id.id.toJavaUUID()
                it[shortName] = helper.shortName
            }
            helper
        }.mapLeft { exception ->
            val message = exception.message
            when {
                message == null -> throw exception
                message.contains("HELPERS_SHORT_NAME_UNIQUE_INDEX") -> HelpersError.Create.DuplicateShortName(helper.shortName)
                else -> throw exception
            }
        }
    }
}
