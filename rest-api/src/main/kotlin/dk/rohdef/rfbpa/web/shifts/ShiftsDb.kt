package dk.rohdef.rfbpa.web.shifts

import arrow.core.Either
import dk.rohdef.axpclient.AxpToDomainMapper
import dk.rohdef.helperplanning.SalarySystemRepository
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftId
import dk.rohdef.rfbpa.configuration.RfBpaConfig
import dk.rohdef.rfbpa.web.DatabaseConnection
import dk.rohdef.rfbpa.web.persistance.helpers.HelpersTable
import dk.rohdef.rfweeks.YearWeek
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
import org.koin.ktor.ext.inject

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
    val weekPlansRepository: SalarySystemRepository by inject()
    val axpToDomainMapper: AxpToDomainMapper by inject()

    get("/qq") {
        val bn = axpToDomainMapper.shiftIdToAxpBooking(
            ShiftId(
                UUID("c2b38da7-4c58-426e-a70e-2777020317ee")
            )
        )

        when (bn) {
            is Either.Right -> call.respond("hello: ${bn.value}")
            is Either.Left -> call.respond("not happy")
        }
    }

    get("/play") {
        val s = weekPlansRepository.shifts(YearWeek(2024, 30))
            .map { it.allShifts.map { Shi.fromShift(it) } }

        when(s) {
            is Either.Right -> call.respond(s.value)
            is Either.Left -> call.respond("Error: ${s.value}")
        }
    }

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
data class Shi(
    val id: UUID,
    val start: String,
    val end: String,
) {
    companion object {
        fun fromShift(shift: Shift): Shi {
            val s = shift.start
            val e = shift.end
            return Shi(
                shift.shiftId.id,
                "${s.year}-W${s.week}-${s.dayOfWeek.value}T${s.time.hour}:${s.time.minute}",
                "${e.time.hour}:${e.time.minute}",
            )
        }
    }
}

@Serializable
data class Hel(
    val id: UUID,
    val name: String,
)
