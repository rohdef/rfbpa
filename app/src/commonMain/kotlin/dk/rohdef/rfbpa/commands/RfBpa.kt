package dk.rohdef.rfbpa.commands

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import dk.rohdef.helperplanning.shifts.WeekPlanRepository
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekRange
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
            single {
                RfBpa(get())
                    .subcommands(
                        UnbookedShifts(get()),
                        HoursWorked(get(), get(named("helpers"))),
                        BookShift(get(), get(named("helpers"))),
                        CreateShift(get()),
                    )
            }
        }
    }
}

const val durationSeparator = "/"
const val durationFormat = "The duration should be in the format yyyy-Www${durationSeparator}yyyy-Www, where yyyy is a year such as [2023] and ww is a week number such as [31]"
fun RawArgument.toYearWeekRange(): ProcessedArgument<YearWeekRange, YearWeekRange> {
    return this.convert {
        if (!it.contains(durationSeparator)) { fail("$durationFormat. The separator [$durationSeparator] was not found.") }

        val timespecifications = it.trim()
            .split("$durationSeparator")
            .map { it.trim() } // TODO not allowed in the format, do we want?
        if (timespecifications.size > 2) { fail("$durationFormat. Too many separators [$durationSeparator] given, only one expected.") }

        val first = timespecifications[0]
        val last = timespecifications[1]
        if (first.isBlank()) { fail("$durationFormat. First week specification is empty.") }
        if (last.isBlank()) { fail("$durationFormat. Last week specification is empty.") }

        val fullSpec = "^[0-9]{4}-W[0-9]{2}(-[0-9])?$".toRegex()
        val shortSpec = "^[0-9]{4}-W[0-9]{2}$".toRegex()
        if (first.matches(fullSpec)) {
            if (!last.matches(fullSpec)) { fail("$durationFormat. Last part must be in full yyyy-Www specification when the first is. Last part was [$last] and first part was [$first].") }

            val firstParts = first.split("-")
                .map { it.trim() }
            val lastParts = last.split("-")
                .map { it.trim() }

            val firstYear = firstParts[0].toInt()
            val firstWeek = firstParts[1].substring(1).toInt()
            val lastYear = lastParts[0].toInt()
            val lastWeek = lastParts[1].substring(1).toInt()

            YearWeek(firstYear, firstWeek)..YearWeek(lastYear, lastWeek)
        } else if (first.matches(shortSpec)) {
            if (!last.matches(shortSpec)) { fail("$durationFormat. Last part must be in short ww specification when the first is. Last part was [$last] and first part was [$first].") }

            val firstWeek = first.toInt()
            val lastWeek = last.toInt()
            val currentYear = 2024
            YearWeek(currentYear, firstWeek)..YearWeek(currentYear, lastWeek)
        } else {
            fail("$durationFormat. First part didn't match any valid specification. First part was [$first] last was [$last].")
        }
    }
}

// TODO improve with domain errors
fun String.toWeekYear(): Either<Throwable, YearWeek> {
    if (!contains("W")) {
        return IllegalArgumentException("Text must contain a 'W' to be a proper week data format").left()
    }

    // 0, 1 and 2 is legitimate, examples
    // - 2024W123 (2024, week 12, Wednesday)
    // - 2024W12 (2024, week 12)
    // - 2024-W12 (2024, week 12)
    if (count { it == '-' } >= 3) {
        return IllegalArgumentException("Contains more than 2 delimiters").left()
    }

    val noDelimiterString = replace("-", "")

    if (noDelimiterString.length < 7) {
        return IllegalArgumentException("Bad length of text for a week date format, text is too short").left()
    } else if (noDelimiterString.length == 8) {
        // TODO better error, better logging, refer to "DayOfWeekYear"
        println("Warning, day of week is ignored for WeekYear")
    } else if (noDelimiterString.length > 8) {
        return IllegalArgumentException("Bad length of text for a week date format, text is too long").left()
    }

    val year = try {
        noDelimiterString.substring(0, 4).toInt()
    } catch (e: NumberFormatException) {
        return IllegalArgumentException("Could not parse year part as a number - first four character must be a valid year", e).left()
    }
    val week = try {
        noDelimiterString.substring(5, 7).toInt()
    } catch (e: NumberFormatException) {
        return IllegalArgumentException("Could not parse week part as a number - there must be two integers after the 'W'", e).left()
    }

    return YearWeek(year, week).right()
}
