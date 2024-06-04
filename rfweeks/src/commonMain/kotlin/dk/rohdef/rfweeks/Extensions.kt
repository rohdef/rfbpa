package dk.rohdef.rfweeks

import kotlinx.datetime.LocalDate

fun LocalDate.toYearWeekDay(): YearWeekDay = YearWeekDay.from(this)
