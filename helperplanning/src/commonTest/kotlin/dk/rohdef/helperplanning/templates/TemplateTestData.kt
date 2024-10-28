package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelperTestData
import dk.rohdef.helperplanning.helpers.HelperTestData.helperIdNamespace
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import dk.rohdef.rfweeks.YearWeekDayAtTime
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

object TemplateTestData {
    private object Helpers {
        val jazz = HelperReservation.Helper(HelperTestData.permanentJazz.shortName)
        val hiphop = HelperReservation.Helper(HelperTestData.permanentHipHop.shortName)
        val blues = HelperReservation.Helper(HelperTestData.permanentBlues.shortName)
        val metal = HelperReservation.Helper(HelperTestData.permanentMetal.shortName)
        val rockabilly = HelperReservation.Helper(HelperTestData.permanentRockabilly.shortName)
    }

    object ShiftTemplates {
        val shift6_30to22_30 = ShiftTemplate(
            Helpers.jazz,
            LocalTime(6, 30),
            LocalTime(22, 30),
        )

        val shift5_00to19_00_unbooked = ShiftTemplate(
            HelperReservation.NoReservation,
            LocalTime(5, 0),
            LocalTime(19, 0),
        )

        val shift5_00to19_00_booked = ShiftTemplate(
            Helpers.hiphop,
            LocalTime(5, 0),
            LocalTime(19, 0),
        )

        val shift14_15to22_15 = ShiftTemplate(
            Helpers.blues,
            LocalTime(14, 15),
            LocalTime(22, 15),
        )

        val shift1_00to6_00 = ShiftTemplate(
            Helpers.metal,
            LocalTime(1, 0),
            LocalTime(6, 0),
        )

        val shiftCrossingDate = ShiftTemplate(
            Helpers.rockabilly,
            LocalTime(22, 30),
            LocalTime(6, 30),
        )

        val monday_day = shift6_30to22_30

        val tuesday_day = shift5_00to19_00_unbooked
        val tueday_night = shiftCrossingDate

        val wednesday_day = shift14_15to22_15

        val thursday_night = shift1_00to6_00

        val saturday_day = shift5_00to19_00_unbooked

        val sunday_day = shift5_00to19_00_booked
    }

    val shiftIdNamespace = UUID("ffe95790-1bc3-4283-8988-7c16809ac47d")

    fun HelperReservation.Helper.asHelper() = HelperTestData.helperId(this.id)

    /**
     * This assumes no overlap in shift start/end pairs
     */
    fun generateTestShiftId(start: YearWeekDayAtTime, end: YearWeekDayAtTime): ShiftId {
        val idText = "$start--$end"

        return ShiftId(
            UUID.generateUUID(shiftIdNamespace, idText)
        )
    }

    class TestRepositoryShifts(yearWeek: YearWeek) {
        fun ShiftTemplate.toShift(yearWeekDay: YearWeekDay): Shift {
            val end = if (this.end < this.start) {
                yearWeekDay.nextDay()
            } else {
                yearWeekDay
            }.atTime(this.end)

            val helperBooking = when (helper) {
                is HelperReservation.Helper -> HelperBooking.Booked(helper.asHelper())
                HelperReservation.NoReservation -> HelperBooking.NoBooking
            }

            val start = yearWeekDay.atTime(this.start)
            return Shift(
                helperBooking,
                generateTestShiftId(start, end),
                start,
                end,
            )
        }

        val monday_day = ShiftTemplates.monday_day.toShift(yearWeek.atDayOfWeek(DayOfWeek.MONDAY))

        val tuesday_day = ShiftTemplates.tuesday_day.toShift(yearWeek.atDayOfWeek(DayOfWeek.TUESDAY))
        val tueday_night = ShiftTemplates.tueday_night.toShift(yearWeek.atDayOfWeek(DayOfWeek.TUESDAY))

        val wednesday_day = ShiftTemplates.wednesday_day.toShift(yearWeek.atDayOfWeek(DayOfWeek.WEDNESDAY))

        val thursday_night = ShiftTemplates.thursday_night.toShift(yearWeek.atDayOfWeek(DayOfWeek.THURSDAY))

        val saturday_day = ShiftTemplates.saturday_day.toShift(yearWeek.atDayOfWeek(DayOfWeek.SATURDAY))

        val sunday_day = ShiftTemplates.sunday_day.toShift(yearWeek.atDayOfWeek(DayOfWeek.SUNDAY))
    }

    object WeekTemplates {
        val complexWeek = WeekTemplate(
            "complex week bookings",
            mapOf(
                DayOfWeek.MONDAY to listOf(
                    ShiftTemplates.monday_day,
                ),

                DayOfWeek.TUESDAY to listOf(
                    ShiftTemplates.tuesday_day,
                    ShiftTemplates.tueday_night,
                ),

                DayOfWeek.WEDNESDAY to listOf(
                    ShiftTemplates.wednesday_day,
                ),

                DayOfWeek.THURSDAY to listOf(
                    ShiftTemplates.thursday_night
                ),

                DayOfWeek.FRIDAY to listOf(),

                DayOfWeek.SATURDAY to listOf(
                    ShiftTemplates.saturday_day
                ),

                DayOfWeek.SUNDAY to listOf(
                    ShiftTemplates.sunday_day
                ),
            )
        )

        val template_week1_monday = WeekTemplate(
            "week 1",

            mapOf(
                DayOfWeek.MONDAY to listOf(
                    ShiftTemplates.monday_day,
                ),
                DayOfWeek.TUESDAY to listOf(),
                DayOfWeek.WEDNESDAY to listOf(),
                DayOfWeek.THURSDAY to listOf(),
                DayOfWeek.FRIDAY to listOf(),
                DayOfWeek.SATURDAY to listOf(),
                DayOfWeek.SUNDAY to listOf(),
            ),
        )

        val template_week2_wednesday = WeekTemplate(
            "week 2",
            mapOf(
                DayOfWeek.MONDAY to listOf(),
                DayOfWeek.TUESDAY to listOf(),
                DayOfWeek.WEDNESDAY to listOf(
                    ShiftTemplates.wednesday_day
                ),
                DayOfWeek.THURSDAY to listOf(),
                DayOfWeek.FRIDAY to listOf(),
                DayOfWeek.SATURDAY to listOf(),
                DayOfWeek.SUNDAY to listOf(),
            ),
        )

        val template_week3_saturday = WeekTemplate(
            "week 3",
            mapOf(
                DayOfWeek.MONDAY to listOf(),
                DayOfWeek.TUESDAY to listOf(),
                DayOfWeek.WEDNESDAY to listOf(),
                DayOfWeek.THURSDAY to listOf(),
                DayOfWeek.FRIDAY to listOf(),
                DayOfWeek.SATURDAY to listOf(
                    ShiftTemplates.saturday_day,
                ),
                DayOfWeek.SUNDAY to listOf(),
            ),
        )
    }

    object Templates {
        val template_single_week_template_complex = Template(
            YearWeek(1919, 3),
            listOf(WeekTemplates.complexWeek),
        )

        val template_three_week_rotation_simple = Template(
            YearWeek(1919, 3),
            listOf(
                WeekTemplates.template_week1_monday,
                WeekTemplates.template_week2_wednesday,
                WeekTemplates.template_week3_saturday,
            ),
        )

        val template_monday = Template(
            YearWeek(1919, 3),
            listOf(WeekTemplates.template_week1_monday)
        )

        val template_wednesday = Template(
            YearWeek(1919, 3),
            listOf(WeekTemplates.template_week2_wednesday)
        )
    }
}
