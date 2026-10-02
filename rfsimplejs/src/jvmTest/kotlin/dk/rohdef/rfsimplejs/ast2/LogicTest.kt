package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.EnclosedExpression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.logic.TernaryExpression
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LogicTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("Ternary expressions") {
        test("simple") {
            val code = "true ? 1 : 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TernaryExpression(
                    Boolean.True,
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("utilize a bit") {
            val code = "a === b ? foo : '2'"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TernaryExpression(
                    LogicExpression.NonCoercingEquals(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    Identifier("foo"),
                    StringLiteral("2"),
                ),
            )
        }

        test("utilize a lot") {
            val code = "(a === b?list[1]:list[0]) > 42 ? (foo?a:'3') : '2'"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TernaryExpression(
                    LogicExpression.GreaterThan(
                        EnclosedExpression(
                            TernaryExpression(
                                LogicExpression.NonCoercingEquals(
                                    Identifier("a"),
                                    Identifier("b"),
                                ),
                                ArrayReference(
                                    Identifier("list"),
                                    Number.SimpleNumber.IntegerNumber(1),
                                ),
                                ArrayReference(
                                    Identifier("list"),
                                    Number.SimpleNumber.IntegerNumber(0),
                                ),
                            ),
                        ),
                        Number.SimpleNumber.IntegerNumber(42),
                    ),
                    EnclosedExpression(
                        TernaryExpression(
                            Identifier("foo"),
                            Identifier("a"),
                            StringLiteral("3"),
                        ),
                    ),
                    StringLiteral("2"),
                ),
            )
        }
    }

    context("If statements") {
    }
})