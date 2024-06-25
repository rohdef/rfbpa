package dk.rohdef.rfbpa.web

import dk.rohdef.rfbpa.web.calendar.CalendarTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConnection {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:mem:dataservice;DB_CLOSE_DELAY=-1"
        Database.connect(jdbcURL, driverClassName)

        transaction {
            SchemaUtils.create(CalendarTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
