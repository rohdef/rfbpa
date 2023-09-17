package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.helperplanning.shifts.YearWeek
import dk.rohdef.helperplanning.shifts.YearWeekRange
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.Closeable

class RfBpa(
    private val weekPlansRepository: WeekPlanRepository
) : CliktCommand(), Closeable {
    override fun run() {}

    override fun close() {
        if (weekPlansRepository is Closeable) weekPlansRepository.close()
    }

    companion object {
        val module = module {
            single<RfBpa> {
                RfBpa(get())
                    .subcommands(
                        UnbookedShifts(get()),
                        HoursWorked(get(), get(named("helpers"))),
                        BookShift(get(), get(named("helpers"))),
                    )
            }
        }
    }
}

val durationFormat = "The duration should be in the format yyyy:ww..yyyy:ww or ww..ww, where yyyy is a year such as [2023] and ww is a week number such as [31]"
fun RawArgument.toYearWeekRange(): ProcessedArgument<YearWeekRange, YearWeekRange> {
    return this.convert {
        if (!it.contains("..")) { fail("$durationFormat. The separator [..] was not found.") }

        val timespecifications = it.trim()
            .split("..")
            .map { it.trim() }
        if (timespecifications.size > 2) { fail("$durationFormat. Too many separators [..] given, only one expected.") }

        val first = timespecifications[0]
        val last = timespecifications[1]
        if (first.isBlank()) { fail("$durationFormat. First week specification is empty.") }
        if (last.isBlank()) { fail("$durationFormat. Last week specification is empty.") }

        val fullSpec = "^[0-9]{4}:[0-9]{2}$".toRegex()
        val shortSpec = "^[0-9]{2}$".toRegex()
        if (first.matches(fullSpec)) {
            if (!last.matches(fullSpec)) { fail("$durationFormat. Last part must be in full yyyy:ww specification when the first is. Last part was [$last] and first part was [$first].") }

            val firstParts = first.split(":")
                .map { it.trim() }
            val lastParts = last.split(":")
                .map { it.trim() }

            val firstYear = firstParts[0].toInt()
            val firstWeek = firstParts[1].toInt()
            val lastYear = lastParts[0].toInt()
            val lastWeek = lastParts[1].toInt()
            YearWeek(firstYear, firstWeek)..YearWeek(lastYear, lastWeek)
        } else if (first.matches(shortSpec)) {
            if (!last.matches(shortSpec)) { fail("$durationFormat. Last part must be in short ww specification when the first is. Last part was [$last] and first part was [$first].") }

            val firstWeek = first.toInt()
            val lastWeek = last.toInt()
            val currentYear = 2023
            YearWeek(currentYear, firstWeek)..YearWeek(currentYear, lastWeek)
        } else {
            fail("$durationFormat. First part didn't match any valid specification. First part was [$first] last was [$last].")
        }
    }
}