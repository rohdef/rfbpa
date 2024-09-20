package dk.rohdef.helperplanning

import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import arrow.core.toNonEmptySetOrNull
import io.kotest.assertions.arrow.core.shouldBeRight

object PrincipalsTestData {
    object FiktivusMaximus {
        val subject = RfbpaPrincipal.Subject("f1kt1vus")
        val name = RfbpaPrincipal.Name("Fiktivus Maximus")
        val email = RfbpaPrincipal.Email("fiktivus@rfbpa.dk")

        fun principal(roles: NonEmptySet<RfbpaPrincipal.RfbpaRoles>) = RfbpaPrincipal(
            subject,
            name,
            email,
            roles,
        )

        val helperAdmin = principal(
            nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.HELPER_ADMIN),
        ).shouldBeRight()

        val shiftAdmin = principal(
            nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN),
        ).shouldBeRight()

        val templateAdmin = principal(
            nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN, RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN),
        ).shouldBeRight()

        val allRoles = principal(
            RfbpaPrincipal.RfbpaRoles.entries.toNonEmptySetOrNull()!!,
        ).shouldBeRight()
    }

    object RealisMinimalis {
        val subject = RfbpaPrincipal.Subject("r34l1s")
        val name = RfbpaPrincipal.Name("Realis Minimalis")
        val email = RfbpaPrincipal.Email("realis@rfbpa.dk")

        fun principal(roles: NonEmptySet<RfbpaPrincipal.RfbpaRoles>) = RfbpaPrincipal(
            subject,
            name,
            email,
            roles,
        )

        val helperAdmin = FiktivusMaximus.principal(
            nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.HELPER_ADMIN),
        ).shouldBeRight()

        val shiftAdmin = principal(
            nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN),
        ).shouldBeRight()

        val templateAdmin = principal(
            nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN, RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN),
        ).shouldBeRight()

        val allRoles = principal(
            RfbpaPrincipal.RfbpaRoles.entries.toNonEmptySetOrNull()!!,
        ).shouldBeRight()
    }
}
