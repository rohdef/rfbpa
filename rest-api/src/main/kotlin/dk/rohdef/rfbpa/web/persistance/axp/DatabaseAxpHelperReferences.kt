package dk.rohdef.rfbpa.web.persistance.axp

//@OptIn(ExperimentalUuidApi::class)
//class DatabaseAxpHelperReferences : AxpHelperReferences {
//    val log = KotlinLogging.logger {}
//
//    override suspend fun bookings(): List<HelperIdMapping> = dbQuery {
//        AxpHelperReferenceTable
//            .selectAll()
//            .map { it.toHelperIdMapping() }
//    }
//
//    override suspend fun helperByTid(tid: HelperTID): Either<FindIdMappingError.HelperTidNotFound, HelperIdMapping> =
//        dbQuery {
//            AxpHelperReferenceTable
//                .selectAll()
//                .where { helperTid eq tid.value }
//                .map { it.toHelperIdMapping() }
//                .singleOrNone()
//                .toEither { FindIdMappingError.HelperTidNotFound(tid) }
//        }
//
//    override suspend fun helperByNumber(number: HelperNumber): Either<FindIdMappingError.HelperNumberNotFound, HelperIdMapping> =
//        dbQuery {
//            AxpHelperReferenceTable
//                .selectAll()
//                .where { helperNumber eq number.value }
//                .map { it.toHelperIdMapping() }
//                .singleOrNone()
//                .toEither { FindIdMappingError.HelperNumberNotFound(number) }
//        }
//
//    override suspend fun helperById(id: HelperId): Either<FindIdMappingError.HelperIdNotFound, HelperIdMapping> =
//        dbQuery {
//            AxpHelperReferenceTable
//                .selectAll()
//                .where { helperId eq id.value }
//                .map { it.toHelperIdMapping() }
//                .singleOrNone()
//                .toEither { FindIdMappingError.HelperIdNotFound(id) }
//        }
//
//    override suspend fun createHelperReference(
//        number: HelperNumber,
//        tid: HelperTID,
//        id: HelperId
//    ): Either<Unit, HelperId> = dbQuery {
//        AxpHelperReferenceTable
//            .upsert(
//                helperNumber,
//                onUpdate = {
//                    it[helperNumber] = number.value
//                    it[helperTid] = tid.value
//                },
//            ) {
//                it[helperNumber] = number.value
//                it[helperTid] = tid.value
//                it[helperId] = id.value
//            }
//        id.right()
//    }
//
//    override suspend fun createHelperReference(number: HelperNumber, id: HelperId): Either<Unit, HelperId> = dbQuery {
//        AxpHelperReferenceTable
//            .upsert(
//                helperNumber,
//                onUpdate = {
//                    it[helperNumber] = number.value
//                },
//            ) {
//                it[helperNumber] = number.value
//                it[helperId] = id.value
//            }
//
//        id.right()
//    }
//
//    private fun ResultRow.toHelperIdMapping() =
//        HelperIdMapping(
//            this[helperId].let { HelperId(it) },
//            this[helperTid]?.let { HelperTID(it) },
//            this[helperNumber].let { HelperNumber(it) },
//        )
//}