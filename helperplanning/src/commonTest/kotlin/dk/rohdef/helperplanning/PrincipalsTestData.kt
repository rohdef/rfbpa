package dk.rohdef.helperplanning

object PrincipalsTestData {
    object FiktivusMaximus {
        val subject = RfbpaPrincipal.Subject("f1kt1vus")
        val name = "Fiktivus Maximus"
        val email = "fiktivus@rfbpa.dk"

        val noRoles = RfbpaPrincipal(
            subject,
            name,
            email,
            listOf(),
        )

        val shiftAdmin = noRoles.copy(
            roles = listOf(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN),
        )

        val templateAdmin = noRoles.copy(
            roles = listOf(RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN),
        )

        val allRoles = noRoles.copy(
            roles = RfbpaPrincipal.RfbpaRoles.entries,
        )
    }

    object RealisMinimalis {
        val subject = RfbpaPrincipal.Subject("r34l1s")
        val name = "Realis Minimalis"
        val email = "realis@rfbpa.dk"

        val noRoles = RfbpaPrincipal(
            subject,
            name,
            email,
            listOf(),
        )

        val shiftAdmin = noRoles.copy(
            roles = listOf(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN),
        )

        val templateAdmin = noRoles.copy(
            roles = listOf(RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN),
        )

        val allRoles = noRoles.copy(
            roles = RfbpaPrincipal.RfbpaRoles.entries,
        )
    }
}
