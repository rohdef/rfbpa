package dk.rohdef.rfbpa.web.persistance.axp

import arrow.core.Either
import arrow.core.singleOrNone
import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.rfbpa.web.DatabaseConnection.dbQuery
import dk.rohdef.rfbpa.web.persistance.axp.AxpHelperReferenceTable.helperId
import dk.rohdef.rfbpa.web.persistance.axp.AxpHelperReferenceTable.helperNumber
import dk.rohdef.rfbpa.web.persistance.axp.AxpHelperReferenceTable.helperTid
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class DatabaseAxpHelperReferences : AxpHelperReferences {
    val log = KotlinLogging.logger {}

    override suspend fun all(): List<HelperIdMapping> = dbQuery {
        AxpHelperReferenceTable
            .selectAll()
            .map { it.toHelperIdMapping() }
    }

    override suspend fun helperByTid(tid: HelperTID): Either<Unit, HelperIdMapping> = dbQuery {
        AxpHelperReferenceTable
            .selectAll()
            .where { helperTid eq tid.value }
            .map { it.toHelperIdMapping() }
            .singleOrNone()
            .toEither {}
    }

    override suspend fun helperByNumber(number: HelperNumber): Either<Unit, HelperIdMapping> = dbQuery {
        AxpHelperReferenceTable
            .selectAll()
            .where { helperNumber eq number.value }
            .map { it.toHelperIdMapping() }
            .singleOrNone()
            .toEither {}
    }

    override suspend fun helperById(id: HelperId): Either<Unit, HelperIdMapping> = dbQuery {
        AxpHelperReferenceTable
            .selectAll()
            .where { helperId eq id.value.toJavaUuid() }
            .map { it.toHelperIdMapping() }
            .singleOrNone()
            .toEither {}
    }

    override suspend fun createHelperReference(tid: HelperTID, number: HelperNumber): Either<Unit, HelperId> {
        dbQuery {
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
                    it[helperId] = HelperId.generateId().value.toJavaUuid()
                }
        }
        return helperByNumber(number)
            .map { it.helperId }
    }

    override suspend fun createHelperReference(number: HelperNumber): Either<Unit, HelperId> {
        dbQuery {
            AxpHelperReferenceTable
                .upsert(
                    helperNumber,
                    onUpdate = {
                        it[helperNumber] = number.value
                    },
                ) {
                    it[helperNumber] = number.value
                    it[helperId] = HelperId.generateId().value.toJavaUuid()
                }
        }

        return helperByNumber(number)
            .map { it.helperId }
    }

    private fun ResultRow.toHelperIdMapping() =
        HelperIdMapping(
            this[helperId].let { HelperId(it.toKotlinUuid()) },
            this[helperTid]?.let { HelperTID(it) },
            this[helperNumber].let { HelperNumber(it) },
        )
}