package dk.rohdef.helperplanning.helpers

sealed interface Helper {
    val id: HelperId
    val name: String

    data class Permanent(
        override val name: String,
        val shortName: String,
        override val id: HelperId = HelperId.generateId(),
    ) : Helper

    data class Temp(
        override val name: String,
        override val id: HelperId = HelperId.generateId(),
    ) : Helper

    data class Unknown(
        override val name: String,
        override val id: HelperId = HelperId.generateId(),
    ) : Helper
}
