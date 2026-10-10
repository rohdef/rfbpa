package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.BlockStatement
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.FunctionExpression
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Lambdas : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("One shot lambda") {
        test("by traditional function") {
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
})