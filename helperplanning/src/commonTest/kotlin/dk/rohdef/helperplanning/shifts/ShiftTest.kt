package dk.rohdef.helperplanning.shifts

import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.rfweeks.YearWeekDayAtTime
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec

class ShiftTest : FunSpec({
    val start = YearWeekDayAtTime.parseUnsafe("2024-W24-1T10:00")
    val end = YearWeekDayAtTime.parseUnsafe("2024-W24-1T12:00")

    test("start must be before end") {
        val afterEnd = YearWeekDayAtTime.parseUnsafe("2024-W24-1T13:00")

        val success = Shift.create(
            HelperBooking.NoBooking,
            start,
            end,
        )
        success.shouldBeRight()

        val failure = Shift.create(
            HelperBooking.NoBooking,
            afterEnd,
            end,
        )
        failure.shouldBeLeft(Shift.ShiftError.StartAfterEnd(afterEnd, end))
    }

    test("Does not allow an illness reference from without illness registration") {
        val illnessReference = Reference.From(ShiftId.generateId(), Reference.LinkType.ILLNESS)
        val booked = HelperBooking.Booked(HelperId.generateId())

        val success = Shift.create(
            booked,
            ShiftId.generateId(),
            start,
            end,
            listOf(Registration.Illness),
            listOf(illnessReference),
        )
        success.shouldBeRight()

        val failure = Shift.create(
            HelperBooking.NoBooking,
            ShiftId.generateId(),
            start,
            end,
            listOf(),
            listOf(illnessReference),
        )
        failure.shouldBeLeft(Shift.ShiftError.IllnessReferenceWithoutIllnessRegistration)
    }

    test("Does not allow an illness registration without being booked") {
        val booked = HelperBooking.Booked(HelperId.generateId())

        val success = Shift.create(
            booked,
            ShiftId.generateId(),
            start,
            end,
            listOf(Registration.Illness),
            listOf(),
        )
        success.shouldBeRight()

        val failure = Shift.create(
            HelperBooking.NoBooking,
            ShiftId.generateId(),
            start,
            end,
            listOf(Registration.Illness),
            listOf(),
        )
        failure.shouldBeLeft(Shift.ShiftError.IllnessRegistrationWithoutBooking)
    }
})