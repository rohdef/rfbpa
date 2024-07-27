package dk.rohdef.rfbpa.web.shifts

import arrow.core.Either
import arrow.core.NonEmptyList
import dk.rohdef.helperplanning.ShiftRepository
import dk.rohdef.helperplanning.shifts.Shift
import dk.rohdef.helperplanning.shifts.ShiftsError
import dk.rohdef.helperplanning.shifts.WeekPlan
import dk.rohdef.rfweeks.YearWeek
import dk.rohdef.rfweeks.YearWeekInterval
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class ShiftsDbKtTest : FunSpec({
    // TODO: 26/07/2024 rohdef - change to call actual shifts endpoint
    val url = "/qq"

    fun FunSpec.restTest(name: String, block: suspend ApplicationTestBuilder.()->Unit) {
        test(name) {
            testApplication(block)
        }
    }
    fun FunSpec.xrestTest(name: String, block: suspend ApplicationTestBuilder.()->Unit) {
        xtest(name) {
            testApplication(block)
        }
    }

    beforeEach {
        startKoin {
            module {
                single< ShiftRepository> {
                    object : ShiftRepository {
                        override suspend fun shifts(yearWeeks: YearWeekInterval): Either<NonEmptyList<ShiftsError>, List<WeekPlan>> {
                            TODO("Test override")
                        }

                        override suspend fun shifts(yearWeek: YearWeek): Either<ShiftsError, WeekPlan> {
                            TODO("Test override")
                        }

                        override suspend fun createShift(shift: Shift): Either<ShiftsError, Shift> {
                            TODO("Test override")
                        }
                    } }
            }
        }
    }

    afterEach {
        stopKoin()
    }

    restTest("No shifts gives an empty list") {
        // TODO probably do nothing
        // query empty week

        val response = client.get(url)

        response.status shouldBe HttpStatusCode.OK
        val weekPlans: List<String> = response.body()
        weekPlans.shouldBeEmpty()
    }

    restTest("No shifts gives an empty list") {
        // TODO add items to system - maybe lift to all tests
        // query multiple weeks

        val response = client.get(url)

        response.status shouldBe HttpStatusCode.OK
        val weekPlans: List<String> = response.body()
        weekPlans shouldBe listOf(
            "dummy for now",
        )
    }

    xrestTest("Authentication from salary system error should be communicated to the client") {
        val response = client.get(url)

        response.status shouldBe HttpStatusCode.Unauthorized
        // TODO: 26/07/2024 rohdef - how do we model a decent error?
        val error: String = response.body()
    }

    xrestTest("Sync and shift error should be communicated to the client as server failure") {
        // TODO: 26/07/2024 rohdef - set the status for error

        val response = client.get(url)

        response.status shouldBe HttpStatusCode.InternalServerError
        // TODO: 26/07/2024 rohdef - how do we model a decent error?
        val error: String = response.body()
    }

    xrestTest("Query params missing") {}
    xrestTest("Query params start is missing") {}
    xrestTest("Query params start is malformed") {}
    xrestTest("Query params end is missing") {}
    xrestTest("Query params end is malformed") {}
    xrestTest("Query params end is before start") {}
})
