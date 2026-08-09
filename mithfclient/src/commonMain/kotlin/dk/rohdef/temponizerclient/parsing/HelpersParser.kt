@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.temponizerclient.parsing

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import dk.rohdef.helperplanning.salary_helper.SalaryHelper
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HelpersParser {
    fun parse(body: String): List<SalaryHelper> {
        val helpers = Ksoup.parse(body)
            .body()
            .select("#main_vikar_list div.worker-pool-list__grid__row")

        return helpers.map { helper(it) }
    }

    private fun helper(helperRow: Element): SalaryHelper {
        val mithfId = helperRow.attr("data-worker-id")

        val children = helperRow.children()
        val personE = children.get(0)
//        val phone = children.get(1)
        val emailE = children.get(2)
//        val hours = children.get(3)
//        val affiliation = children.get(4)

//        personE.rem
//        TODO("Deal with the missing removeAll... possibly a PR for upstream?")
//        personE.removeAll { it.hasClass("tn-d-block") && it.hasClass("tn-d-lg-none-imp") }
//        emailE.removeAll { it.hasClass("tn-d-block") && it.hasClass("tn-d-lg-none-imp") }

        val name = personE.ownText()

        val helperNumber = personE.firstElementChild()!!.ownText()

        val email = emailE.ownText()

        return SalaryHelper(
            Uuid.random(),
            name,
            helperNumber,
            mithfId,
            email,
        )
    }
}