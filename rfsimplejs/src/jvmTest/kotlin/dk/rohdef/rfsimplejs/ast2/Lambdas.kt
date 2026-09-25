package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.BlockStatement
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.FunctionExpression
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class Lambdas {
    private val grammar = JavaScriptGrammar::class.createGrammar()
    private val parser = Parser(grammar)

    @Nested
    inner class `Oneshot lambda` {
        @Test
        fun `oneshot lambda`() {
            val text = "(function() {})()"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program(
                FunctionCall(
                    FunctionExpression(
                        FunctionExpression.Parameters(),
                        BlockStatement(),
                    ),
                ),
            )
        }
    }
}