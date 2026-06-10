package dk.rohdef.rfbpa.web.persistance

import dk.rohdef.rfbpa.web.calendar.CalendarTable
import dk.rohdef.rfbpa.web.persistance.axp.ShiftReferenceTable
import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import dk.rohdef.rfbpa.web.persistance.shifts.ReferencesTable
import dk.rohdef.rfbpa.web.persistance.shifts.RegistrationsTable
import dk.rohdef.rfbpa.web.persistance.shifts.ShiftBookingsTable
import dk.rohdef.rfbpa.web.persistance.shifts.ShiftsTable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

object TestDatabaseConnection {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:mem:${UUID.generateUUID()};DB_CLOSE_DELAY=-1"
        Database.connect(jdbcURL, driverClassName)

        transaction {
            SchemaUtils.create(ShiftReferenceTable)
            SchemaUtils.create(CalendarTable)
            SchemaUtils.create(HelpersTable)
            SchemaUtils.create(ShiftsTable)
            SchemaUtils.create(ShiftBookingsTable)
            SchemaUtils.create(RegistrationsTable)
            SchemaUtils.create(ReferencesTable)
        }
    }

    fun disconnect() {
        TransactionManager.defaultDatabase?.let { TransactionManager.closeAndUnregister(it) }
    }
}
