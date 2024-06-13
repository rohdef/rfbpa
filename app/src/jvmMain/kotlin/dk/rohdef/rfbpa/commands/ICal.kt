package dk.rohdef.rfbpa.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import dk.rohdef.helperplanning.WeekPlanRepository
import dk.rohdef.helperplanning.shifts.HelperBooking
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import java.io.Closeable

class ICal(
    private val weekPlansRepository: WeekPlanRepository,
    private val helpers: Map<String, String>,
) : CliktCommand() {
    private val log = KotlinLogging.logger {}

    private val helper by argument()
        .convert { HelperBooking.PermanentHelper(helpers[it]!!) }
        .help("ID of the helper to request data for")

    private val yearWeekInterval by argument()
        .toYearWeekInterval()

    override fun run() = runBlocking {
        log.info { "Creating ical" }


        embeddedServer(Netty, port = 8080) {
            install(CORS)
            install(Authentication) {
                basic("calendar") {
                    realm = "RF BPA calendar function"
                    validate { credentials ->
                        if (credentials.name == "rff" && credentials.password == "rff") {
                            UserIdPrincipal(credentials.name)
                        } else {
                            null
                        }
                    }
                }
            }

            routing {
                get("/") {
                    call.respondText("Hello, world!")
                }
                authenticate("calendar") {
                    get("/calendar") {
                        call.respondText("Calendar!")
                    }
                }
                get("/health") {
                    call.respondText("I am healthy!")
                }
            }
        }.start(wait = true)


//        val calendar = Calendar()
//            // TODO how free is this? Ugly format so far
//            .withProdId("-//Rohde Fischer//RF-BPA//DA")
//            .withDefaults()
//            .fluentTarget
//
//        val ug = RandomUidGenerator()
//
//        either {
//            val weekPlans = weekPlansRepository
//                .shifts(yearWeekInterval)
//                .bind()
//
//            val shifts = weekPlans
//                .allShifts
//                .shifts
//                .filter {
//                    val hid = it.helperId
//                    (hid is HelperBooking.PermanentHelper) && (hid.axpId == "1")
//                }
//
//            val calendarEvents = shifts.map {
//                VEvent(
//                    it.start.toJavaLocalDateTime(),
//                    it.end.toJavaLocalDateTime(),
//                    "Arbejde"
//                )
//                    .withProperty(ug.generateUid())
//                    .getFluentTarget<VEvent>()
//            }
////            val freshlendar = calendarEvents.fold(calendar as FluentCalendar) { calendar, event ->
////                calendar.withComponent(event)
////            }.fluentTarget
//            calendarEvents.forEach { calendar.add<Calendar>(it) }
//
//            val mmm = Files.createFile(Paths.get("./helper.ical"))
//            mmm.writeText(calendar.toString())
//        }


        currentContext.parent?.command.let {
            if (it is Closeable) it.close()
        }
    }
}
