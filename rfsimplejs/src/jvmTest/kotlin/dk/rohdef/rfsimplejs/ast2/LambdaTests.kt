package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.BlockStatement
import dk.rohdef.rfsimplejs.ast2.statement.EnclosedExpression
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.FunctionExpression
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Lambdas")
class LambdaTests : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("one shot") {
        test("by traditional function") {
            val text = "(function() {})()"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program(
                FunctionCall(
                    EnclosedExpression(
                        FunctionExpression(
                            FunctionExpression.Parameters(),
                            BlockStatement(),
                        ),
                    ),
                ),
            )
        }

        test("by actual lambda") {
            val text = "(() => 42)()"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program(
                FunctionCall(
                    EnclosedExpression(
                        FunctionExpression(
                            FunctionExpression.Parameters(),
                            BlockStatement(),
                        ),
                    ),
                ),
            )
        }
    }
})