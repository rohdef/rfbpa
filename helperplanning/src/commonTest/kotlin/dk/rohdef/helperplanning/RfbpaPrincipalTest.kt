package dk.rohdef.helperplanning

import arrow.core.nonEmptySetOf
import arrow.core.toNonEmptySetOrNull
import com.marcinmoskala.math.powerset
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData

class RfbpaPrincipalTest : FunSpec({
    context("subject") {}

    context("name") {}

    context("email") {}

    context("roles") {
        // TODO: 19/09/2024 rohdef - https://kotest.io/docs/proptest/property-based-testing.html probably better, powerset gets slow
        context("${RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN} combined with") {
            val roleUnderTest = RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN
            val remainingRoles = RfbpaPrincipal.RfbpaRoles.entries - roleUnderTest
            val powersetOfRemaining = remainingRoles.powerset()
                .map { it.toNonEmptySetOrNull() }
                .filterNotNull()

            withData(
                nameFn = { it.joinToString(",") },
                powersetOfRemaining,
            ) {
                RfbpaPrincipal(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    PrincipalsTestData.FiktivusMaximus.name,
                    PrincipalsTestData.FiktivusMaximus.email,
                    it + roleUnderTest,
                ).shouldBeRight()
            }
        }

        context("${RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN} combined with") {
            val roleUnderTest = RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN
            val remainingRoles = RfbpaPrincipal.RfbpaRoles.entries - roleUnderTest
            val powersetOfRemaining = remainingRoles.powerset().filterNot { it.isEmpty() }
                .map { it.toNonEmptySetOrNull() }
                .filterNotNull()
            val powerSetWithShiftadmin = powersetOfRemaining.filter { it.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN) }
            val powerSetWithoutShiftadmin = powersetOfRemaining.filterNot { it.contains(RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN) }

            withData(
                nameFn = { it.joinToString(",") },
                powerSetWithShiftadmin,
            ) {
                RfbpaPrincipal(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    PrincipalsTestData.FiktivusMaximus.name,
                    PrincipalsTestData.FiktivusMaximus.email,
                    it + roleUnderTest,
                ).shouldBeRight()
            }

            withData(
                nameFn = { it.joinToString(",") },
                powerSetWithoutShiftadmin,
            ) {
                RfbpaPrincipal(
                    PrincipalsTestData.FiktivusMaximus.subject,
                    PrincipalsTestData.FiktivusMaximus.name,
                    PrincipalsTestData.FiktivusMaximus.email,
                    it + roleUnderTest,
                ) shouldBeLeft RfbpaPrincipal.Error.MissingRole(
                    nonEmptySetOf(RfbpaPrincipal.RfbpaRoles.TEMPLATE_ADMIN, RfbpaPrincipal.RfbpaRoles.SHIFT_ADMIN),
                    it + roleUnderTest,
                )
            }
        }
    }
})
