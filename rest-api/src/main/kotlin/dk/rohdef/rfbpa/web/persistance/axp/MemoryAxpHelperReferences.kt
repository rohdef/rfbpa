package dk.rohdef.rfbpa.web.persistance.axp

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId

// TODO: 25/06/2024 rohdef - delete once proper database actions available
// TODO: 27/10/2024 rohdef - considerations needed for optionality, current way is messy
class MemoryAxpHelperReferences(
    helpers: List<HelperDataBaseItem>,
) : AxpHelperReferences {
    private val helpersConverted = helpers.map {
        HelperIdMapping(
            HelperId(it.id),
            HelperTID(it.helperTid),
            HelperNumber(it.helperNumber),
        )
    }

    override fun all(): List<HelperIdMapping> {
        return helpersConverted
    }

    override fun helperByTid(tid: HelperTID): Either<Unit, HelperIdMapping> {
        return helpersConverted.find {
            it.axpTid == tid
        }?.right() ?: Unit.left()
    }

    override fun helperByNumber(number: HelperNumber): Either<Unit, HelperIdMapping> {
        return helpersConverted.find {
            it.axpNumber == number
        }?.right() ?: Unit.left()
    }

    override fun helperById(helperId: HelperId): Either<Unit, HelperIdMapping> {
        return helpersConverted.find {
            it.helperId == helperId
        }?.right() ?: Unit.left()
    }

    override fun createHelperReference(tid: HelperTID, number: HelperNumber): Either<Unit, HelperId> {
        TODO("not implemented - this version is immutable for now")
    }

    override fun createHelperReference(number: HelperNumber): Either<Unit, HelperId> {
        TODO("not implemented - this version is immutable for now")
    }
}
