package dk.rohdef.rfsimplejs.ast2.prefixExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.ChainedCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.PostfixExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Postfix expression")
class PostfixTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("increment (++)") {
        test("identifier") {
            val code = "foo++"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Increment(
                    Identifier("foo"),
                ),
            )
        }

        test("number") {
            val code = "1++"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Increment(
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("chained call") {
            val code = "foo.bar++;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Increment(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    ),
                ),
            )
        }

        test("array reference") {
            val code = "foo[0]++"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Increment(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "count   \t \n++;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Increment(
                    Identifier("count"),
                ),
            )
        }
    }

    context("decrement (--)") {
        test("identifier") {
            val code = "foo--"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Decrement(
                    Identifier("foo"),
                ),
            )
        }

        test("number") {
            val code = "1--"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Decrement(
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("chained call") {
            val code = "foo.bar--;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Decrement(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    ),
                ),
            )
        }

        test("array reference") {
            val code = "foo[0]--"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Decrement(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "count   \t \n--;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PostfixExpression.Decrement(
                    Identifier("count"),
                ),
            )
        }
    }
})