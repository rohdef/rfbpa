package dk.rohdef.rfbpa.web.persistance.helpers

import arrow.core.Either
import arrow.core.Either.Companion.catchOrThrow
import arrow.core.firstOrNone
import dk.rohdef.helperplanning.helpers.HelpersRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersError
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class DatabaseHelpers : HelpersRepository {
    private val log = KotlinLogging.logger {}

    private fun rowToHelper(row: ResultRow): Helper {
        return Helper.Permanent(
            row[HelpersTable.name],
            row[HelpersTable.shortName]!!,
            HelperId(row[HelpersTable.id].toKotlinUUID()),
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

    override suspend fun byShortName(shortName: String): Either<HelpersError.CannotFindHelperByShortName, Helper.Permanent> =
        dbQuery {
            HelpersTable
                .selectAll()
                .where { HelpersTable.shortName eq shortName }
                // TODO: 26/10/2024 rohdef - find a nicer way, even though this is a given
                .map { rowToHelper(it) as Helper.Permanent }
                .firstOrNone()
                .toEither { HelpersError.CannotFindHelperByShortName(shortName) }
        }

    override suspend fun create(helper: Helper): Either<HelpersError.Create, Helper> = dbQuery {
        catchOrThrow<Exception, Helper> {
            HelpersTable.upsert {
                it[id] = helper.id.id.toJavaUUID()

                when (helper) {
                    is Helper.Permanent -> {
                        it[name] = helper.name
                        it[shortName] = helper.shortName
                    }
                    is Helper.Temp -> {
                        it[name] = helper.name
                    }
                    is Helper.Unknown -> {
                        it[name] = helper.name
                    }
                }
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
