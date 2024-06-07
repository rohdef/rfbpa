package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.shifts.ShiftType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

object TemplateTestData {
    object ShiftTemplates {
        val shift6_30to22_30 = ShiftTemplate(
            HelperReservation.Helper("jazz"),
            ShiftType.DAY,
            LocalTime(6, 30),
            LocalTime(22, 30),
        )

        val shift5_00to19_00 = ShiftTemplate(
            HelperReservation.NoReservation,
            ShiftType.DAY,
            LocalTime(5, 0),
            LocalTime(19, 0),
        )

        val shift14_15to22_15 = ShiftTemplate(
            HelperReservation.Helper("blues"),
            ShiftType.EVENING,
            LocalTime(14, 15),
            LocalTime(22, 15),
        )

        val shift1_00to6_00 = ShiftTemplate(
            HelperReservation.Helper("metal"),
            ShiftType.NIGHT,
            LocalTime(1, 0),
            LocalTime(6, 0),
        )

        val shiftCrossingDate = ShiftTemplate(
            HelperReservation.Helper("rockabilly"),
            ShiftType.NIGHT,
            LocalTime(22, 30),
            LocalTime(6, 30),
        )

        val monday_day = shift6_30to22_30

        val tuesday_day = shift5_00to19_00
        val tueday_night = shiftCrossingDate

        val wednesday_day = shift14_15to22_15

        val thursday_night = shift1_00to6_00
    }

    object WeekTemplates {
        val week = listOf(
            WeekTemplate(
                "",
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

                    DayOfWeek.FRIDAY to listOf(
                        ShiftTemplates.shift6_30to22_30
                    ),

                    DayOfWeek.SATURDAY to listOf(
                        ShiftTemplates.shift6_30to22_30
                    ),

                    DayOfWeek.SUNDAY to listOf(
                        ShiftTemplates.shift6_30to22_30
                    ),
                )
            )
        )
    }
}
