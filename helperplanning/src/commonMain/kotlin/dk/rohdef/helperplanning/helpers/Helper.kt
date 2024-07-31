package dk.rohdef.helperplanning.helpers

data class Helper(
    val id: HelperId = HelperId.generateId(),
    val shortName: String,
)
