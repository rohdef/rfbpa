package dk.rohdef.rfbpa.web.persistance.axp

import arrow.core.Either
import arrow.core.right
import arrow.core.singleOrNone
import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.AxpHelperReferences.FindIdMappingError
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import dk.rohdef.rfbpa.web.persistance.axp.AxpHelperReferenceTable.helperId
import dk.rohdef.rfbpa.web.persistance.axp.AxpHelperReferenceTable.helperNumber
import dk.rohdef.rfbpa.web.persistance.axp.AxpHelperReferenceTable.helperTid
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class DatabaseAxpHelperReferences : AxpHelperReferences {
    val log = KotlinLogging.logger {}

    override suspend fun bookings(): List<HelperIdMapping> = dbQuery {
        AxpHelperReferenceTable
            .selectAll()
            .map { it.toHelperIdMapping() }
    }

    override suspend fun helperByTid(tid: HelperTID): Either<FindIdMappingError.HelperTidNotFound, HelperIdMapping> =
        dbQuery {
            AxpHelperReferenceTable
                .selectAll()
                .where { helperTid eq tid.value }
                .map { it.toHelperIdMapping() }
                .singleOrNone()
                .toEither { FindIdMappingError.HelperTidNotFound(tid) }
        }

    override suspend fun helperByNumber(number: HelperNumber): Either<FindIdMappingError.HelperNumberNotFound, HelperIdMapping> =
        dbQuery {
            AxpHelperReferenceTable
                .selectAll()
                .where { helperNumber eq number.value }
                .map { it.toHelperIdMapping() }
                .singleOrNone()
                .toEither { FindIdMappingError.HelperNumberNotFound(number) }
        }

    override suspend fun helperById(id: HelperId): Either<FindIdMappingError.HelperIdNotFound, HelperIdMapping> =
        dbQuery {
            AxpHelperReferenceTable
                .selectAll()
                .where { helperId eq id.value }
                .map { it.toHelperIdMapping() }
                .singleOrNone()
                .toEither { FindIdMappingError.HelperIdNotFound(id) }
        }

    override suspend fun createHelperReference(
        number: HelperNumber,
        tid: HelperTID,
        id: HelperId
    ): Either<Unit, HelperId> = dbQuery {
        AxpHelperReferenceTable
            .upsert(
                helperNumber,
                onUpdate = {
                    it[helperNumber] = number.value
                    it[helperTid] = tid.value
                },
            ) {
                it[helperNumber] = number.value
                it[helperTid] = tid.value
                it[helperId] = id.value
            }
        id.right()
    }

    override suspend fun createHelperReference(number: HelperNumber, id: HelperId): Either<Unit, HelperId> = dbQuery {
        AxpHelperReferenceTable
            .upsert(
                helperNumber,
                onUpdate = {
                    it[helperNumber] = number.value
                },
            ) {
                it[helperNumber] = number.value
                it[helperId] = id.value
            }

        id.right()
    }

    private fun ResultRow.toHelperIdMapping() =
        HelperIdMapping(
            this[helperId].let { HelperId(it) },
            this[helperTid]?.let { HelperTID(it) },
            this[helperNumber].let { HelperNumber(it) },
        )
}