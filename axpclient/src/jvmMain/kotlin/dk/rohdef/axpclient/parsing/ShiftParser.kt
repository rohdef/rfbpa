package dk.rohdef.axpclient.parsing

import arrow.core.*
import dk.rohdef.helperplanning.shifts.HelperBooking
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftData
import dk.rohdef.rfsimplejs.JavaScriptParser
import dk.rohdef.rfsimplejs.ast.*
import kotlinx.datetime.LocalDateTime
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ShiftParser {
    private val jsParser = JavaScriptParser()

    fun parse(elements: Elements): ShiftData {
        return if (elements.isEmpty()) {
            ShiftData.NoData
        } else {
            parseShifts(elements)
        }
    }

    private fun parseShifts(elements: Elements): ShiftData.Shifts {
        return ShiftData.Shifts(
            elements
                .map { parseShift(it) }
                .toNonEmptyListOrNull()!!
        )
    }

    private fun parseShift(element: Element): Shift {
        val mouseover = element.attr("onmouseover")

        val tooltipParameter = jsParser.parse(mouseover)
            .functionCalls()
            .first { it.name == Name("toolTip") }
            .parameters as Text

        val shiftData = parseTooltip(tooltipParameter.text)

        return Shift(
            shiftData.getValue(AxpField.HELPER_ID).map { HelperBooking.PermanentHelper(it) }
                .getOrElse { HelperBooking.NoBooking },
            shiftData.getValue(AxpField.SHIFT_ID).getOrElse { throw IllegalStateException("Shift ID is missing in $shiftData\n$tooltipParameter") },
            shiftData.getValue(AxpField.SHIFT_START).getOrElse { TODO() }.let { toLocalDateTime(it) },
            shiftData.getValue(AxpField.SHIFT_END).getOrElse { TODO() }.let { toLocalDateTime(it) },
        )
    }
}

private fun Expression.expressions() =
    when (this) {
        is Expressions -> this.expressions
        else -> TODO("Handle errors/unexpected types")
    }

private fun Expression.functionCalls() =
    this.expressions()
        .filterIsInstance<FunctionCall>()

private fun parseTooltip(tooltip: String): Map<AxpField, Option<String>> {
    val tooltipEntries = tooltip
        // TODO bad fix for inconsistent code
        .replace("OBS! Konflikt med 48 timers regel<br />", "")
        .split("<br>")

    val pairs = tooltipEntries.filter { it.contains(':') }
        .map { it.split(":", limit = 2) }
        .associate { it[0].trim() to it[1].trim() }
        .mapKeys { toAxpField(it.key) }
        .mapValues { Some(it.value) }

    val standAlone = tooltipEntries.filter { !it.contains(':') }
        .map { it.trim() }

    return (pairs +
            (AxpField.HELPER_CATEGORY to standAlone[0].toOption()) +
            (AxpField.HELPER_NAME to standAlone.getOrNull(1).toOption()))
        .withDefault { None }
}

private fun toAxpField(key: String) =
    when (key) {
        "Vagt type" -> AxpField.SHIFT_TYPE
        "Fra" -> AxpField.SHIFT_START
        "Til" -> AxpField.SHIFT_END
        "Vikarnummer" -> AxpField.HELPER_ID
        "Telefon" -> AxpField.HELPER_PHONE
        "Mobil" -> AxpField.HELPER_MOBILE
        "Ordrenummer" -> AxpField.SHIFT_ID
        else -> throw IllegalArgumentException("Field not recognized from axp data. Field was: $key")
    }

private val axpDateTimePattern = "^([0-9]{2})-([0-9]{2})-([0-9]{4}) ([0-9]{2}):([0-9]{2})".toRegex()
private fun toLocalDateTime(date: String): LocalDateTime =
    date.replace(axpDateTimePattern, "$3-$2-$1T$4:$5")
        .let { LocalDateTime.parse(it) }