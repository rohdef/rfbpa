package dk.rohdef.helperplanning.templates

import dk.rohdef.helperplanning.shifts.ShiftType
import kotlinx.datetime.LocalTime

object TemplateTestData {
    object ShiftTemplates {
        val shift6_30to22_30 = ShiftTemplate(
            HelperReservation.Helper("jazz"),
            ShiftType.DAY,
            LocalTime(6, 30),
            LocalTime(22, 30),
        )

        val shiftCrossingDate = ShiftTemplate(
            HelperReservation.Helper("rockabilly"),
            ShiftType.NIGHT,
            LocalTime(22, 30),
            LocalTime(6, 30),
        )
    }
}
