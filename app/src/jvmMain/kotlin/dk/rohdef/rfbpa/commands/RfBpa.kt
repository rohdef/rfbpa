package dk.rohdef.rfbpa.commands

import arrow.core.Either
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import dk.rohdef.helperplanning.WeekPlanRepository
import dk.rohdef.helperplanning.helpers.Helper
import dk.rohdef.rfbpa.HelperDataBaseItem
import dk.rohdef.rfweeks.YearWeekInterval
import dk.rohdef.rfweeks.YearWeekIntervalParseError
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
                val helpers = get<Map<String, HelperDataBaseItem>>(named("helpers"))
                    .mapValues { Helper.ID(it.value.id) }

                RfBpa(get())
                    .subcommands(
                        UnbookedShifts(get()),
                        BookShift(get(), helpers),
                        CreateShift(get()),
                        ApplyTemplate(get()),
                    )
            }
        }
    }
}

fun RawArgument.toYearWeekInterval(): ProcessedArgument<YearWeekInterval, YearWeekInterval> {
    fun mapError(yearWeekIntervalParseError: YearWeekIntervalParseError): String {
//        if (!it.contains(durationSeparator)) { fail("$durationFormat. The separator [$durationSeparator] was not found.") }
//        if (timespecifications.size > 2) { fail("$durationFormat. Too many separators [$durationSeparator] given, only one expected.") }

        return "Error parsing date component. ${yearWeekIntervalParseError}"
    }

    return this.convert {
        val yearWeekInterval = YearWeekInterval.parse(it)
            .mapLeft {
                it.map { mapError(it) }
                    .joinToString("\n\n")
            }

        when (yearWeekInterval) {
            is Either.Right -> yearWeekInterval.value
            is Either.Left -> fail(yearWeekInterval.value)
        }
    }
}
