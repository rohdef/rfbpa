@file:OptIn(ExperimentalUuidApi::class)

package dk.rohdef.helperplanning.helpers

import dk.rohdef.helperplanning.generateUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object HelperTestData {
    val helperIdNamespace = Uuid.parse("ffe95790-1bc3-4283-8988-7c16809ac47d")

    fun helperId(id: String) = HelperId(
        Uuid.generateUuid(helperIdNamespace, id)
    )

    fun permanent(name: String, shortName: String) = Helper(
        helperId(shortName),
        name,
        shortName,
    )

    val permanentJazz = permanent("Helper 1", "jazz")
    val permanentHipHop = permanent("Helper 2", "hiphop")
    val permanentBlues = permanent("Helper 2", "blues")
    val permanentMetal = permanent("Helper 2", "metal")
    val permanentRockabilly = permanent("Helper 2", "rockabilly")

    val allHelpers = listOf(
        permanentJazz,
        permanentHipHop,
        permanentBlues,
        permanentMetal,
        permanentRockabilly,
    )
}
