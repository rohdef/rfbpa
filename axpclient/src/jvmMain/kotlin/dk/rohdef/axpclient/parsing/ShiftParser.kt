package dk.rohdef.axpclient.parsing

import arrow.core.*
import dk.rohdef.axpclient.helper.AxpMetadataRepository
import dk.rohdef.axpclient.helper.HelperTID
import dk.rohdef.axpclient.helper.Shift
import dk.rohdef.rfsimplejs.JavaScriptParser
import dk.rohdef.rfsimplejs.ast.*
import kotlinx.datetime.LocalDateTime
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

internal class ShiftParser {
    private val jsParser = JavaScriptParser()

    fun parse(elements: Elements): List<Shift> {
        return elements.map { parseShift(it) }
    }

    private fun parseShift(element: Element): Shift {
        val mouseover = element.attr("onmouseover")

        val tooltipParameter = jsParser.parse(mouseover)
            .functionCalls()
            .first { it.name == Name("toolTip") }
            .parameters as Text

        val shiftData = parseTooltip(tooltipParameter.text)

        // TODO replace with AXP model
        val helperBooking = shiftData.getValue(AxpField.HELPER_ID).map {
            when (it) {
                "60621" -> AxpMetadataRepository.VacancyBooking
                else -> AxpMetadataRepository.PermanentHelper(
                    HelperTID(it),
                )
            }
        }
            .getOrElse { AxpMetadataRepository.NoBooking }
        val axpShiftId = shiftData.getValue(AxpField.SHIFT_ID)
            .map { Shift.AxpShiftId(it) }
            .getOrElse { throw IllegalStateException("Shift ID is missing in $shiftData\n$tooltipParameter") }
        val start = shiftData.getValue(AxpField.SHIFT_START)
            .map { toLocalDateTime(it) }
            .getOrElse { TODO() }
        val end = shiftData.getValue(AxpField.SHIFT_END)
            .map { toLocalDateTime(it) }
            .getOrElse { TODO() }
        return Shift(
            helperBooking,
            axpShiftId,
            start,
            end,
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
