package dk.rohdef.rfbpa.web.persistance

import dk.rohdef.rfbpa.web.calendar.CalendarTable
import dk.rohdef.rfbpa.web.persistance.axp.ShiftReferenceTable
import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import dk.rohdef.rfbpa.web.persistance.shifts.ReferencesTable
import dk.rohdef.rfbpa.web.persistance.shifts.RegistrationsTable
import dk.rohdef.rfbpa.web.persistance.shifts.ShiftBookingsTable
import dk.rohdef.rfbpa.web.persistance.shifts.ShiftsTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object TestDatabaseConnection {
    fun connect() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        Database.connect(jdbcURL, driverClassName)
    }

    fun init() = transaction {
        SchemaUtils.drop(
            ShiftReferenceTable,
            CalendarTable,
            HelpersTable,
            ShiftsTable,
            ShiftBookingsTable,
            RegistrationsTable,
            ReferencesTable
        )

        SchemaUtils.create(
            ShiftReferenceTable,
            CalendarTable,
            HelpersTable,
            ShiftsTable,
            ShiftBookingsTable,
            RegistrationsTable,
            ReferencesTable
        )
    }
}
