package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.BlockStatement
import dk.rohdef.rfsimplejs.ast2.statement.EnclosedExpression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.logic.IfStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.TernaryExpression
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Logic expressions")
class LogicExpressionTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("ternary") {
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

    context("if") {
        test("simple") {
            val code = "if (true) 1 else 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Boolean.True,
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("utilize a bit") {
            val code = "if (a === b) foo else '2'"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
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
            val code = "if ((a === b ? list[1] : list[0]) > 42) if (foo) a else '3' else '2'"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
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
                    IfStatement(
                        Identifier("foo"),
                        Identifier("a"),
                        StringLiteral("3"),
                    ),
                    StringLiteral("2"),
                ),
            )
        }

        test("simple without else") {
            val code = "if (true) 1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Boolean.True,
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("utilize a bit without else") {
            val code = "if (a === b) foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    LogicExpression.NonCoercingEquals(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    Identifier("foo"),
                ),
            )
        }

        test("then block without else") {
            val code = "if (a === b) { foo }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    LogicExpression.NonCoercingEquals(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    BlockStatement(
                        Identifier("foo"),
                    ),
                ),
            )
        }

        test("empty then block without else") {
            val code = "if (true) {}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Boolean.True,
                    BlockStatement(),
                ),
            )
        }

        test("then block and else statement") {
            val code = "if (true) { 1 } else 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Boolean.True,
                    BlockStatement(
                        Number.SimpleNumber.IntegerNumber(1),
                    ),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("then statement and else block") {
            val code = "if (true) 1 else { 2 }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Boolean.True,
                    Number.SimpleNumber.IntegerNumber(1),
                    BlockStatement(
                        Number.SimpleNumber.IntegerNumber(2),
                    ),
                ),
            )
        }

        test("then block and else block") {
            val code = "if (a === b) { foo } else { '2' }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    LogicExpression.NonCoercingEquals(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    BlockStatement(
                        Identifier("foo"),
                    ),
                    BlockStatement(
                        StringLiteral("2"),
                    ),
                ),
            )
        }

        test("empty then block and empty else block") {
            val code = "if (true) {} else {}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Boolean.True,
                    BlockStatement(),
                    BlockStatement(),
                ),
            )
        }

        test("multiple statements in then and else blocks") {
            val code = """
                if (a === b) {
                    foo;
                    1;
                } else {
                    '2';
                    bar;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    LogicExpression.NonCoercingEquals(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    BlockStatement(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(1),
                    ),
                    BlockStatement(
                        StringLiteral("2"),
                        Identifier("bar"),
                    ),
                ),
            )
        }
    }

    xcontext("for") {
    }

    xcontext("while") {
    }

    xcontext("switch") {
    }
})