package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.logic.ForStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.IfStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.WhileStatement
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Break and Continue statements")
class BreakAndContinueTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("toCode") {
        test("Break toCode") {
            Break.toCode() shouldBe "break;"
        }

        test("Continue toCode") {
            Continue.toCode() shouldBe "continue;"
        }
    }

    context("standalone statements") {
        test("standalone break with semicolon") {
            val code = "break;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(Break)
        }

        test("standalone break without semicolon") {
            val code = "break"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(Break)
        }

        test("standalone continue with semicolon") {
            val code = "continue;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(Continue)
        }

        test("standalone continue without semicolon") {
            val code = "continue"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(Continue)
        }

        test("break followed by continue") {
            val code = """
                break;
                continue;
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Break,
                Continue,
            )
        }
    }

    context("while loops") {
        test("break in while block") {
            val code = """
                while (true) {
                    break;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                WhileStatement(
                    Boolean.True,
                    BlockStatement(Break),
                ),
            )
        }

        test("continue in while block") {
            val code = """
                while (true) {
                    continue;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                WhileStatement(
                    Boolean.True,
                    BlockStatement(Continue),
                ),
            )
        }

        test("break in single statement while") {
            val code = "while (true) break;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                WhileStatement(
                    Boolean.True,
                    Break,
                ),
            )
        }

        test("continue in single statement while") {
            val code = "while (true) continue;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                WhileStatement(
                    Boolean.True,
                    Continue,
                ),
            )
        }
    }

    context("for loops") {
        test("break in for loop block") {
            val code = """
                for (let i = 0; i < 10; i++) {
                    break;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForStatement(
                    Declaration.Let(
                        Identifier("i") to Number.SimpleNumber.IntegerNumber(0),
                    ),
                    LogicExpression.LessThan(
                        Identifier("i"),
                        Number.SimpleNumber.IntegerNumber(10),
                    ),
                    PostfixExpression.Increment(
                        Identifier("i"),
                    ),
                    BlockStatement(Break),
                ),
            )
        }

        test("continue in for loop block") {
            val code = """
                for (let i = 0; i < 10; i++) {
                    continue;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForStatement(
                    Declaration.Let(
                        Identifier("i") to Number.SimpleNumber.IntegerNumber(0),
                    ),
                    LogicExpression.LessThan(
                        Identifier("i"),
                        Number.SimpleNumber.IntegerNumber(10),
                    ),
                    PostfixExpression.Increment(
                        Identifier("i"),
                    ),
                    BlockStatement(Continue),
                ),
            )
        }

        test("break in single statement for loop") {
            val code = "for (let i = 0; i < 10; i++) break;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForStatement(
                    Declaration.Let(
                        Identifier("i") to Number.SimpleNumber.IntegerNumber(0),
                    ),
                    LogicExpression.LessThan(
                        Identifier("i"),
                        Number.SimpleNumber.IntegerNumber(10),
                    ),
                    PostfixExpression.Increment(
                        Identifier("i"),
                    ),
                    Break,
                ),
            )
        }

        test("continue in single statement for loop") {
            val code = "for (let i = 0; i < 10; i++) continue;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForStatement(
                    Declaration.Let(
                        Identifier("i") to Number.SimpleNumber.IntegerNumber(0),
                    ),
                    LogicExpression.LessThan(
                        Identifier("i"),
                        Number.SimpleNumber.IntegerNumber(10),
                    ),
                    PostfixExpression.Increment(
                        Identifier("i"),
                    ),
                    Continue,
                ),
            )
        }
    }

    context("complex loop control flow") {
        test("while loop with multiple statements, if conditions, break and continue") {
            val code = """
                while (running) {
                    count = count + 1;
                    if (count > 10) {
                        break;
                    }
                    if (count === 5) {
                        continue;
                    }
                    process(count);
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                WhileStatement(
                    Identifier("running"),
                    BlockStatement(
                        Assignment(
                            Identifier("count"),
                            ArithmeticExpression.Addition(
                                Identifier("count"),
                                Number.SimpleNumber.IntegerNumber(1),
                            ),
                        ),
                        IfStatement(
                            LogicExpression.GreaterThan(
                                Identifier("count"),
                                Number.SimpleNumber.IntegerNumber(10),
                            ),
                            BlockStatement(Break),
                        ),
                        IfStatement(
                            LogicExpression.NonCoercingEquals(
                                Identifier("count"),
                                Number.SimpleNumber.IntegerNumber(5),
                            ),
                            BlockStatement(Continue),
                        ),
                        FunctionCall(
                            Identifier("process"),
                            listOf(Identifier("count")),
                        ),
                    ),
                ),
            )
        }
    }
})