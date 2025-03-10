package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.web.calendar.CalendarTable
import dk.rohdef.rfbpa.web.persistance.axp.ShiftReferenceTable
import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import dk.rohdef.rfbpa.web.persistance.shifts.ShiftBookingsTable
import dk.rohdef.rfbpa.web.persistance.shifts.ShiftsTable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConnection {
    val log = KotlinLogging.logger {}

    fun init() {
        log.info { "Initilizing database connection" }
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:mem:rfbpa;DB_CLOSE_DELAY=-1"
        Database.connect(jdbcURL, driverClassName)
        log.debug {  "Connected to database at: $jdbcURL" }
        log.debug {  "Connected with driver: $driverClassName" }

        val h2Tcp = Server.createTcpServer(
            "-tcpAllowOthers",
        ).start()
        log.debug { "DB tcp on: ${h2Tcp.port}" }
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info { "Closing DB" }
            h2Tcp.shutdown()
        })

        transaction {
            log.debug { "Creating tables" }
            SchemaUtils.create(CalendarTable)
            SchemaUtils.create(HelpersTable)
            SchemaUtils.create(ShiftsTable)
            SchemaUtils.create(ShiftBookingsTable)
            SchemaUtils.create(ShiftReferenceTable)
            log.debug { "Tables created" }
        }

        log.debug { "Transaction ended" }
    }

    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
