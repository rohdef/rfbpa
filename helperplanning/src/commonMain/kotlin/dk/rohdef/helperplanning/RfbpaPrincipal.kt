package dk.rohdef.helperplanning

data class RfbpaPrincipal(
    val subject: Subject,
    val name: String,
    val email: String,
    val roles: List<RfbpaRoles>,
) {
    @JvmInline
    value class Subject(
        val value: String,
    )

    enum class RfbpaRoles {
        SHIFT_ADMIN,
        TEMPLATE_ADMIN,
    }
}
