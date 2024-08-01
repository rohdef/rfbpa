package dk.rohdef.rfbpa.web.persistance.helpers

import dk.rohdef.helperplanning.helpers.HelperId
import dk.rohdef.helperplanning.helpers.HelpersError
import dk.rohdef.rfbpa.web.persistance.TestDatabaseConnection
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class DatabaseHelpersTest : FunSpec({
    val helperRepository = DatabaseHelpers()

    beforeEach {
        TestDatabaseConnection.init()
    }

    afterEach {
        TestDatabaseConnection.disconnect()
    }

    test("Get all hellpers") {
        helperRepository.all() shouldBe emptyList()

        helperRepository.create(TestHelpers.fiktivus)
        helperRepository.create(TestHelpers.realis)

        helperRepository.all() shouldContainExactlyInAnyOrder listOf(
            TestHelpers.fiktivus,
            TestHelpers.realis,
        )
    }

    test("Get by id") {
        helperRepository.create(TestHelpers.fiktivus)
        helperRepository.create(TestHelpers.realis)

        helperRepository.byId(TestHelpers.fiktivus.id) shouldBeRight TestHelpers.fiktivus
        helperRepository.byId(TestHelpers.realis.id) shouldBeRight TestHelpers.realis
        val randomHelperId = HelperId.generateId()
        helperRepository.byId(randomHelperId) shouldBeLeft HelpersError.CannotFindHelperById(randomHelperId)
    }

    test("Get by short name") {
        helperRepository.create(TestHelpers.fiktivus)
        helperRepository.create(TestHelpers.realis)

        helperRepository.byShortName(TestHelpers.fiktivus.shortName) shouldBeRight TestHelpers.fiktivus
        helperRepository.byShortName(TestHelpers.realis.shortName) shouldBeRight TestHelpers.realis
        helperRepository.byShortName("nynne") shouldBeLeft HelpersError.CannotFindHelperByShortName("nynne")
    }

    test("Does not allow duplicate short name") {
        helperRepository.create(TestHelpers.fiktivus)

        helperRepository.create(TestHelpers.fiktivus.copy(id = HelperId.generateId())) shouldBeLeft HelpersError.Create.DuplicateShortName(TestHelpers.fiktivus.shortName)
    }
})
