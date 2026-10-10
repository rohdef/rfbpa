package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class Statements {
    private val grammar = JavaScriptGrammar::class.createGrammar()
    private val parser = Parser(grammar)

    @Nested
    inner class Expressions {
        @Test
        fun `empty string`() {
            val text = ""

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }
    }
}