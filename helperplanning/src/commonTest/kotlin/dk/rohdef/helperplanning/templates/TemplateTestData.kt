package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.MemoryWeekPlanRepository
import dk.rohdef.helperplanning.shifts.ShiftType
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekDay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

object TemplateTestData {
    object Helpers {
        val jazz = HelperReservation.Helper("jazz")
        val hiphop = HelperReservation.Helper("hiphop")
        val blues = HelperReservation.Helper("blues")
        val metal = HelperReservation.Helper("metal")
        val rockabilly = HelperReservation.Helper("rockabilly")

        val allHelpers = listOf(
            jazz,
            hiphop,
            blues,
            metal,
            rockabilly
        )

        val helpersMap = allHelpers.associate { it.id to it.id }
    }

    object ShiftTemplates {
        val shift6_30to22_30 = ShiftTemplate(
            Helpers.jazz,
            ShiftType.DAY,
            LocalTime(6, 30),
            LocalTime(22, 30),
        )

        val shift5_00to19_00_unbooked = ShiftTemplate(
            HelperReservation.NoReservation,
            ShiftType.DAY,
            LocalTime(5, 0),
            LocalTime(19, 0),
        )

        val shift5_00to19_00_booked = ShiftTemplate(
            Helpers.hiphop,
            ShiftType.DAY,
            LocalTime(5, 0),
            LocalTime(19, 0),
        )

        val shift14_15to22_15 = ShiftTemplate(
            Helpers.blues,
            ShiftType.EVENING,
            LocalTime(14, 15),
            LocalTime(22, 15),
        )

        val shift1_00to6_00 = ShiftTemplate(
            Helpers.metal,
            ShiftType.NIGHT,
            LocalTime(1, 0),
            LocalTime(6, 0),
        )

        val shiftCrossingDate = ShiftTemplate(
            Helpers.rockabilly,
            ShiftType.NIGHT,
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

    class TestRepositoryShifts(yearWeek: YearWeek) {
        fun ShiftTemplate.toShift(yearWeekDay: YearWeekDay): MemoryWeekPlanRepository.MemoryShift {
            val end = if (this.end < this.start) {
                yearWeekDay.nextDay()
            } else {
                yearWeekDay
            }.atTime(this.end)

            return MemoryWeekPlanRepository.MemoryShift(
                yearWeekDay.atTime(this.start),
                end,
                this.type,
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
    }
}