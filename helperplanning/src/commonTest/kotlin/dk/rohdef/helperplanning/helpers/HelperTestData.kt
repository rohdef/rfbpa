package dk.rohdef.helperplanning.helpers

object HelperTestData {
    val permanentJazz = Helper.Permanent("Helper 1", "jazz")
    val permanentHipHop = Helper.Permanent("Helper 2", "hiphop")
    val permanentBlues = Helper.Permanent("Helper 2", "blues")
    val permanentMetal = Helper.Permanent("Helper 2", "metal")
    val permanentRockabilly = Helper.Permanent("Helper 2", "rockabilly")

    val temp1 = Helper.Temp("Helper 3")
    val temp2 = Helper.Temp("Helper 4")

    val unknown1 = Helper.Unknown("Helper 5")
    val unknown2 = Helper.Unknown("Helper 6")

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
