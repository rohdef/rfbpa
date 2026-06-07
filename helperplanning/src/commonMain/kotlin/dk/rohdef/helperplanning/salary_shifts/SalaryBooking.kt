package dk.rohdef.helperplanning.salary_shifts

import arrow.core.Either
import arrow.core.raise.either
import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.shifts.HelperBooking

sealed interface SalaryBooking {
    suspend fun toBooking(
        findOrCreateBooking: suspend () -> Either<Unit, HelperId>
    ): Either<Unit, HelperBooking>

    object NoBooking : SalaryBooking {
        override suspend fun toBooking(findOrCreateBooking: suspend () -> Either<Unit, HelperId>): Either<Unit, HelperBooking> = either {
            HelperBooking.NoBooking
        }

        override fun toString(): String = "NoBooking"
    }

    @JvmInline
    value class Helper(val helper: HelperId) : SalaryBooking {
        override suspend fun toBooking(findOrCreateBooking: suspend () -> Either<Unit, HelperId>): Either<Unit, HelperBooking> = either {
            HelperBooking.Booked(helper)
        }
    }

    @JvmInline
    value class UnknownHelper(val helper: HelperId) : SalaryBooking {
        override suspend fun toBooking(findOrCreateBooking: suspend () -> Either<Unit, HelperId>): Either<Unit, HelperBooking> = either {
            HelperBooking.Booked(helper)
        }
    }

    object Vacancy : SalaryBooking {
        override suspend fun toBooking(findOrCreateBooking: suspend () -> Either<Unit, HelperId>) = either {
            findOrCreateBooking()
                .bind()
                .let { HelperBooking.Booked(it) }
        }

        override fun toString(): String = "Vacancy"
    }
}
