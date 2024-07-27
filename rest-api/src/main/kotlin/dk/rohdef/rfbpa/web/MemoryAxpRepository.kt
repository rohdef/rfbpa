package dk.rohdef.rfbpa.web

import dk.rohdef.axpclient.AxpRepository
import dk.rohdef.axpclient.helper.HelperIdMapping
import dk.rohdef.axpclient.helper.HelperNumber
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.helperplanning.helpers.Helper

// TODO: 25/06/2024 rohdef - delete once proper database actions available
class MemoryAxpRepository(
    helpers: List<HelperDataBaseItem>,
) : AxpRepository {
    private val helpersConverted = helpers.map {
        HelperIdMapping(
            Helper.ID(it.id),
            HelperTID(it.helperTid),
            HelperNumber(it.helperNumber),
        )
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

    override fun helperById(id: Helper.ID): HelperIdMapping {
        return helpersConverted.find {
            it.helperID == id
        } ?: TODO("not implemented")
    }
}
