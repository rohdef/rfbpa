package dk.rohdef.rfbpa.web.errors

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ErrorDtoTest : FunSpec({
    test("Simple serialization") {
        // TODO add proper hierarchy
        val errorType = UnknownError

        errorType.serialize() shouldBe "Blah.Bluh.UnknownError"
    }

    test("Simple deserialization") {
        // TODO add proper hierarchy
        val searializedErrorType = "Blah.Bluh.UnknownError"
        val errorType = ErrorType.deserialize(searializedErrorType)

        errorType shouldBe UnknownError
    }

    xtest("serializing enum is not allowed") {}

    xtest("serializing class is not allowed") {}
})