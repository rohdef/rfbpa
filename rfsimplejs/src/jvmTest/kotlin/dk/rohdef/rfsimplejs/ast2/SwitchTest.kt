package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.*
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.logic.IfStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.Switch
import dk.rohdef.rfsimplejs.ast2.statement.logic.SwitchCase
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Switch statements")
class SwitchTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("basic switch structure") {
        test("empty switch") {
            val code = "switch (x) {}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    emptyList(),
                ),
            )
        }

        test("single case with empty body") {
            val code = """
                switch (x) {
                    case 1:
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(),
                        ),
                    ),
                ),
            )
        }

        test("single case with single return statement") {
            val code = """
                switch (x) {
                    case 1:
                        return true;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                Return.Expression(Boolean.True),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("single case with multiple statements") {
            val code = """
                switch (x) {
                    case 1:
                        y = 10;
                        return y;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                Assignment(
                                    Identifier("y"),
                                    Number.SimpleNumber.IntegerNumber(10),
                                ),
                                Return.Expression(Identifier("y")),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("single default case with empty body") {
            val code = """
                switch (x) {
                    default:
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Default(
                            BlockStatement(),
                        ),
                    ),
                ),
            )
        }

        test("single default case with statements") {
            val code = """
                switch (x) {
                    default:
                        return 0;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Default(
                            BlockStatement(
                                Return.Expression(Number.SimpleNumber.IntegerNumber(0)),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("multiple cases and ordering") {
        test("multiple match cases without default") {
            val code = """
                switch (action) {
                    case "START":
                        start();
                    case "STOP":
                        stop();
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("action"),
                    listOf(
                        SwitchCase.Match(
                            StringLiteral("START"),
                            BlockStatement(
                                FunctionCall(Identifier("start"), emptyList()),
                            ),
                        ),
                        SwitchCase.Match(
                            StringLiteral("STOP"),
                            BlockStatement(
                                FunctionCall(Identifier("stop"), emptyList()),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("multiple match cases with default at end") {
            val code = """
                switch (status) {
                    case 200:
                        return "OK";
                    case 404:
                        return "Not Found";
                    default:
                        return "Unknown";
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("status"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(200),
                            BlockStatement(
                                Return.Expression(StringLiteral("OK")),
                            ),
                        ),
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(404),
                            BlockStatement(
                                Return.Expression(StringLiteral("Not Found")),
                            ),
                        ),
                        SwitchCase.Default(
                            BlockStatement(
                                Return.Expression(StringLiteral("Unknown")),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("default case at beginning") {
            val code = """
                switch (x) {
                    default:
                        return 0;
                    case 1:
                        return 1;
                    case 2:
                        return 2;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Default(
                            BlockStatement(
                                Return.Expression(Number.SimpleNumber.IntegerNumber(0)),
                            ),
                        ),
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                Return.Expression(Number.SimpleNumber.IntegerNumber(1)),
                            ),
                        ),
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(2),
                            BlockStatement(
                                Return.Expression(Number.SimpleNumber.IntegerNumber(2)),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("default case in middle") {
            val code = """
                switch (x) {
                    case 1:
                        return "one";
                    default:
                        return "other";
                    case 2:
                        return "two";
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                Return.Expression(StringLiteral("one")),
                            ),
                        ),
                        SwitchCase.Default(
                            BlockStatement(
                                Return.Expression(StringLiteral("other")),
                            ),
                        ),
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(2),
                            BlockStatement(
                                Return.Expression(StringLiteral("two")),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("fallthrough / consecutive case labels") {
        test("consecutive cases sharing a body") {
            val code = """
                switch (day) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        return "weekday";
                    case 6:
                    case 7:
                        return "weekend";
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("day"),
                    listOf(
                        SwitchCase.Match(Number.SimpleNumber.IntegerNumber(1), BlockStatement()),
                        SwitchCase.Match(Number.SimpleNumber.IntegerNumber(2), BlockStatement()),
                        SwitchCase.Match(Number.SimpleNumber.IntegerNumber(3), BlockStatement()),
                        SwitchCase.Match(Number.SimpleNumber.IntegerNumber(4), BlockStatement()),
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(5),
                            BlockStatement(Return.Expression(StringLiteral("weekday"))),
                        ),
                        SwitchCase.Match(Number.SimpleNumber.IntegerNumber(6), BlockStatement()),
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(7),
                            BlockStatement(Return.Expression(StringLiteral("weekend"))),
                        ),
                    ),
                ),
            )
        }

        test("consecutive cases with default fallthrough") {
            val code = """
                switch (x) {
                    case 1:
                    default:
                        return "fallback";
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(Number.SimpleNumber.IntegerNumber(1), BlockStatement()),
                        SwitchCase.Default(
                            BlockStatement(Return.Expression(StringLiteral("fallback"))),
                        ),
                    ),
                ),
            )
        }
    }

    context("switch subjects") {
        test("literal subject") {
            val code = """
                switch (true) {
                    case true:
                        return 1;
                    case false:
                        return 0;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Boolean.True,
                    listOf(
                        SwitchCase.Match(
                            Boolean.True,
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(1))),
                        ),
                        SwitchCase.Match(
                            Boolean.False,
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(0))),
                        ),
                    ),
                ),
            )
        }

        test("arithmetic expression subject") {
            val code = """
                switch (a + b) {
                    case 0:
                        return "zero";
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    ArithmeticExpression.Addition(Identifier("a"), Identifier("b")),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(0),
                            BlockStatement(Return.Expression(StringLiteral("zero"))),
                        ),
                    ),
                ),
            )
        }

        test("comparison expression subject") {
            val code = """
                switch (x === y) {
                    case true:
                        return "equal";
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    LogicExpression.NonCoercingEquals(Identifier("x"), Identifier("y")),
                    listOf(
                        SwitchCase.Match(
                            Boolean.True,
                            BlockStatement(Return.Expression(StringLiteral("equal"))),
                        ),
                    ),
                ),
            )
        }

        test("function call subject") {
            val code = """
                switch (getType(item)) {
                    case "string":
                        return 1;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    FunctionCall(Identifier("getType"), listOf(Identifier("item"))),
                    listOf(
                        SwitchCase.Match(
                            StringLiteral("string"),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(1))),
                        ),
                    ),
                ),
            )
        }

        test("chained call subject") {
            val code = """
                switch (obj.prop.getValue()) {
                    case 42:
                        return true;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    ChainedCall(
                        ChainedCall(
                            Identifier("obj"),
                            Identifier("prop"),
                        ),
                        FunctionCall(Identifier("getValue"), emptyList()),
                    ),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(42),
                            BlockStatement(Return.Expression(Boolean.True)),
                        ),
                    ),
                ),
            )
        }

        test("array reference subject") {
            val code = """
                switch (items[0]) {
                    case "first":
                        return true;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    ArrayReference(
                        Identifier("items"),
                        Number.SimpleNumber.IntegerNumber(0),
                    ),
                    listOf(
                        SwitchCase.Match(
                            StringLiteral("first"),
                            BlockStatement(Return.Expression(Boolean.True)),
                        ),
                    ),
                ),
            )
        }

        test("enclosed expression subject") {
            val code = """
                switch ((x * 2)) {
                    case 10:
                        return 5;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    EnclosedExpression(
                        ArithmeticExpression.Multiplication(
                            Identifier("x"),
                            Number.SimpleNumber.IntegerNumber(2),
                        ),
                    ),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(10),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(5))),
                        ),
                    ),
                ),
            )
        }
    }

    context("various case match expressions") {
        test("string, boolean, null, undefined match expressions") {
            val code = """
                switch (val) {
                    case "text":
                        return 1;
                    case true:
                        return 2;
                    case null:
                        return 3;
                    case undefined:
                        return 4;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("val"),
                    listOf(
                        SwitchCase.Match(
                            StringLiteral("text"),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(1))),
                        ),
                        SwitchCase.Match(
                            Boolean.True,
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(2))),
                        ),
                        SwitchCase.Match(
                            Null,
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(3))),
                        ),
                        SwitchCase.Match(
                            Undefined,
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(4))),
                        ),
                    ),
                ),
            )
        }

        test("regex and template literal match expressions") {
            val code = """
                switch (val) {
                    case /abc/i:
                        return 1;
                    case `hello`:
                        return 2;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("val"),
                    listOf(
                        SwitchCase.Match(
                            RegexLiteral("abc", "i"),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(1))),
                        ),
                        SwitchCase.Match(
                            TemplateLiteral(TemplateLiteral.TemplateLiteralPart.StringPart("hello")),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(2))),
                        ),
                    ),
                ),
            )
        }

        test("identifier and binary expression match") {
            val code = """
                switch (val) {
                    case CONST_VAL:
                        return 1;
                    case a + b:
                        return 2;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("val"),
                    listOf(
                        SwitchCase.Match(
                            Identifier("CONST_VAL"),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(1))),
                        ),
                        SwitchCase.Match(
                            ArithmeticExpression.Addition(Identifier("a"), Identifier("b")),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(2))),
                        ),
                    ),
                ),
            )
        }
    }

    context("body statements inside cases") {
        test("variable declaration and assignment inside case") {
            val code = """
                switch (x) {
                    case 1:
                        const temp = 100;
                        result = temp * 2;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                Declaration.Constant(
                                    mapOf(
                                        Identifier("temp") to Number.SimpleNumber.IntegerNumber(100),
                                    ),
                                ),
                                Assignment(
                                    Identifier("result"),
                                    ArithmeticExpression.Multiplication(
                                        Identifier("temp"),
                                        Number.SimpleNumber.IntegerNumber(2),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("if statement inside case") {
            val code = """
                switch (x) {
                    case 1:
                        if (ready) {
                            return true;
                        }
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                IfStatement(
                                    Identifier("ready"),
                                    BlockStatement(Return.Expression(Boolean.True)),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("nested switch statement") {
            val code = """
                switch (a) {
                    case 1:
                        switch (b) {
                            case 2:
                                return "nested";
                        }
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("a"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                Switch(
                                    Identifier("b"),
                                    listOf(
                                        SwitchCase.Match(
                                            Number.SimpleNumber.IntegerNumber(2),
                                            BlockStatement(Return.Expression(StringLiteral("nested"))),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("switch in various contexts") {
        test("inside function declaration") {
            val code = """
                function evaluate(x) {
                    switch (x) {
                        case 1:
                            return "one";
                        default:
                            return "other";
                    }
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Variable(
                    Identifier("evaluate") to FunctionExpression(
                        FunctionExpression.Parameters().withParameter("x"),
                        BlockStatement(
                            Switch(
                                Identifier("x"),
                                listOf(
                                    SwitchCase.Match(
                                        Number.SimpleNumber.IntegerNumber(1),
                                        BlockStatement(Return.Expression(StringLiteral("one"))),
                                    ),
                                    SwitchCase.Default(
                                        BlockStatement(Return.Expression(StringLiteral("other"))),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("inside arrow function") {
            val code = """
                const fn = (x) => {
                    switch (x) {
                        case 0:
                            return false;
                    }
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Constant(
                    mapOf(
                        Identifier("fn") to FunctionExpression(
                            FunctionExpression.Parameters().withParameter("x"),
                            BlockStatement(
                                Switch(
                                    Identifier("x"),
                                    listOf(
                                        SwitchCase.Match(
                                            Number.SimpleNumber.IntegerNumber(0),
                                            BlockStatement(Return.Expression(Boolean.False)),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("multiple switch statements in program") {
            val code = """
                switch (a) {
                    case 1:
                        return 1;
                }
                switch (b) {
                    default:
                        return 2;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("a"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(1))),
                        ),
                    ),
                ),
                Switch(
                    Identifier("b"),
                    listOf(
                        SwitchCase.Default(
                            BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(2))),
                        ),
                    ),
                ),
            )
        }
    }

    context("toCode") {
        test("empty switch toCode") {
            val switchStatement = Switch(Identifier("x"), emptyList())
            switchStatement.toCode() shouldBe "switch (x) {}"
        }

        test("switch with cases and default toCode") {
            val switchStatement = Switch(
                Identifier("x"),
                listOf(
                    SwitchCase.Match(
                        Number.SimpleNumber.IntegerNumber(1),
                        BlockStatement(Return.Expression(Number.SimpleNumber.IntegerNumber(10))),
                    ),
                    SwitchCase.Default(
                        BlockStatement(Return.Void),
                    ),
                ),
            )

            switchStatement.toCode() shouldBe """
                switch (x) {
                case 1:
                return 10;
                default:
                return;
                }
            """.trimIndent()
        }
    }
})
