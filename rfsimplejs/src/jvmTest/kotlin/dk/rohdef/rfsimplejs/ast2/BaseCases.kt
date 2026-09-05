package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BaseCases : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("whitespace programs") {
        test("empty code") {
            val text = ""

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("spaces") {
            val text = "    "

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("tabs") {
            val text = "\t\t"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("new lines") {
            val text = "\n\n\n"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("mix") {
            val text = "\n\n   \n\t\t\n\r\n\r\r\n\n"

            val result = parser.run(text)

            result.stackTop shouldBe Program()
        }
    }
})