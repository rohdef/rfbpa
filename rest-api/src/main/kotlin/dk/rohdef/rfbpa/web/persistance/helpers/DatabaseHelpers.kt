package dk.rohdef.rfbpa.web.persistance.helpers

import arrow.core.Either
import arrow.core.Either.Companion.catchOrThrow
import arrow.core.firstOrNone
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersError
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class DatabaseHelpers : HelpersRepository {
    private fun rowToHelper(row: ResultRow): Helper {
        return Helper(
            HelperId(row[HelpersTable.id]),
            row[HelpersTable.name],
            row[HelpersTable.shortName]!!,
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
            .where { HelpersTable.id eq helperId.value }
            .map { rowToHelper(it) }
            .firstOrNone()
            .toEither { HelpersError.CannotFindHelperById(helperId) }
    }

    override suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper> =
        dbQuery {
            HelpersTable
                .selectAll()
                .where { HelpersTable.shortName eq shortName }
                // TODO: 26/10/2024 rohdef - find a nicer way, even though this is a given
                .map { rowToHelper(it) }
                .firstOrNone()
                .toEither { HelpersError.CannotFindHelperByShortName(shortName) }
        }

    override suspend fun create(helper: Helper): Either<HelpersError.Create, Helper> = dbQuery {
        catchOrThrow<Exception, Helper> {
            HelpersTable.upsert {
                it[id] = helper.id.value

                it[name] = helper.name
                it[shortName] = helper.shortName
            }
            helper
        }.mapLeft { exception ->
            val message = exception.message
            when {
                message == null -> throw exception
                else -> throw exception
            }
        }
    }
}
