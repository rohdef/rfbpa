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

    fun permanent(name: String, shortName: String) = Helper.Permanent(
        name,
        shortName,
        helperId(shortName),
    )

    fun temp(name: String) = Helper.Temp(
        name,
        helperId(name),
    )

    fun unknown(name: String) = Helper.Unknown(
        name,
        helperId(name),
    )


    val permanentJazz = permanent("Helper 1", "jazz")
    val permanentHipHop = permanent("Helper 2", "hiphop")
    val permanentBlues = permanent("Helper 2", "blues")
    val permanentMetal = permanent("Helper 2", "metal")
    val permanentRockabilly = permanent("Helper 2", "rockabilly")

    val temp1 = temp("Helper 3")
    val temp2 = temp("Helper 4")

    val unknown1 = unknown("Helper 5")
    val unknown2 = unknown("Helper 6")

    val allHelpers = listOf(
        permanentJazz,
        permanentHipHop,
        permanentBlues,
        permanentMetal,
        permanentRockabilly,

        temp1,
        temp2,

        unknown1,
        unknown2,
    )
}
