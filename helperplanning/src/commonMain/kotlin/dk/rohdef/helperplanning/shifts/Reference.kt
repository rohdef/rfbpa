package dk.rohdef.helperplanning.shifts

sealed interface Reference {
    val linkType: LinkType
    val id: ShiftId

    data class To(
        override val id: ShiftId,
        override val linkType: LinkType,
    ) : Reference

    data class From(
        override val id: ShiftId,
        override val linkType: LinkType,
    ) : Reference

    enum class LinkType {
        ILLNESS,
    }
}