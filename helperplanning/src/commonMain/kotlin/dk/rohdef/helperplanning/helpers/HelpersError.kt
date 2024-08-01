package dk.rohdef.helperplanning.helpers

sealed interface HelpersError {
    data class CannotFindHelperById(val id: HelperId) : HelpersError
    data class CannotFindHelperByShortName(val shortName: String) : HelpersError

    sealed interface Create : HelpersError {
        data class DuplicateShortName(val shortName: String) : Create
    }
}
