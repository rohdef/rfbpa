package dk.rohdef.helperplanning

data class RfbpaPrincipal(
    val subject: String,
    val name: String,
    val email: String,
    val roles: List<RfbpaRoles>,
) {
    enum class RfbpaRoles {
        SHIFT_ADMIN,
        TEMPLATE_ADMIN,
    }
}
