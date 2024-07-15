package dk.rohdef.rfbpa.web.shifts

import dk.rohdef.rfbpa.web.DatabaseConnection
import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

suspend fun ins() = DatabaseConnection.dbQuery {
    HelpersTable.insert {
        it[id] = UUID.generateUUID().toJavaUUID()
        it[name] = "Fiktivus Maximus"
    }
    HelpersTable.insert {
        it[id] = UUID.generateUUID().toJavaUUID()
        it[name] = "Realis Minimalis"
    }
}

suspend fun fet(): List<Hel> = DatabaseConnection.dbQuery {
    HelpersTable.selectAll()
        .map {
            Hel(
                it[HelpersTable.id].toKotlinUUID(),
                it[HelpersTable.name],
            )
        }
}

fun Route.dbShifts() {
    get("/seed") {
        ins()

        call.respondText("Seeded")
    }

    get("/db-shifts") {
        val o = fet()
        println(o)

        call.respond(o)
    }

}

@Serializable
data class Hel(
    val id: UUID,
    val name: String,
)
