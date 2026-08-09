package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class Declarations {
    private val grammar = SimpleJavaScript2::class.createGrammar()
    private val parser = Parser(grammar)

    @Nested
    inner class Variable {
        @Test
        fun `no valuae`() {
            val text = "var a"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        @Test
        fun `simple assignment`() {
            val text = "var a = 42"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

    }

    @Nested
    inner class Let {
        @Test
        fun `empty string`() {
            val text = ""

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }
    }

    @Nested
    inner class Constant {
        @Test
        fun `empty string`() {
            val text = ""

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }
    }
}