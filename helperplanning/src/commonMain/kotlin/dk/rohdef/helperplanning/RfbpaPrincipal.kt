package dk.rohdef.helperplanning

import arrow.core.Either
import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import arrow.core.raise.either

data class RfbpaPrincipal private constructor(
    val subject: Subject,
    val name: Name,
    val email: Email,
    val roles: NonEmptySet<RfbpaRoles>,
) {
    fun eitherCopy(
        subject: Subject = this.subject,
        name: Name = this.name,
        email: Email = this.email,
        roles: NonEmptySet<RfbpaRoles> = this.roles,
    ) = invoke(subject, name, email, roles)

    companion object {
        operator fun invoke(
            subject: Subject,
            name: Name,
            email: Email,
            roles: NonEmptySet<RfbpaRoles>,
        ): Either<Error, RfbpaPrincipal> = either {
            if (roles.contains(RfbpaRoles.TEMPLATE_ADMIN)) {
                if (!roles.contains(RfbpaRoles.SHIFT_ADMIN)) {
                    raise(Error.MissingRole(
                        nonEmptySetOf(RfbpaRoles.TEMPLATE_ADMIN, RfbpaRoles.SHIFT_ADMIN),
                        roles,
                    ))
                }
            }

            RfbpaPrincipal(subject, name, email, roles)
        }
    }

    @JvmInline
    value class Subject(
        val value: String,
    )

    @JvmInline
    value class Name(
        val value: String,
    )

    @JvmInline
    value class Email(
        val value: String,
    )

    enum class RfbpaRoles {
        SHIFT_ADMIN,
        TEMPLATE_ADMIN,
        HELPER_ADMIN,
    }

    sealed interface Error {
        data class MissingRole(
            val expectedRoles: NonEmptySet<RfbpaRoles>,
            val actualRoles: NonEmptySet<RfbpaRoles>,
        ) : Error
    }
}
