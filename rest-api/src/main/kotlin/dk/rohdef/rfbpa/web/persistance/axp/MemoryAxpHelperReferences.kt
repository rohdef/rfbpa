package dk.rohdef.rfbpa.web.persistance.axp

import dk.rohdef.axpclient.AxpHelperReferences
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.HelperId

// TODO: 25/06/2024 rohdef - delete once proper database actions available
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

    override fun helperByTid(tid: HelperTID): HelperIdMapping {
        return helpersConverted.find {
            it.axpTid == tid
        } ?: TODO("not implemented")
    }

    override fun helperByNumber(number: HelperNumber): HelperIdMapping {
        return helpersConverted.find {
            it.axpNumber == number
        } ?: TODO("not implemented")
    }

    override fun helperById(helperId: HelperId): HelperIdMapping {
        return helpersConverted.find {
            it.helperId == helperId
        } ?: TODO("not implemented")
    }

    override fun createHelperReference(tid: HelperTID, number: HelperNumber, helperId: HelperId): AxpHelperReferences {
        TODO("not implemented - this version is immutable for now")
    }
}
