package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.Declaration
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.TemplateLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.TemplateLiteral.TemplateLiteralPart.ExpressionPart
import dk.rohdef.rfsimplejs.ast2.statement.literals.TemplateLiteral.TemplateLiteralPart.StringPart
import dk.rohdef.rfsimplejs.ast2.statement.logic.TernaryExpression
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Template literals")
class TemplateLiteralTests : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("text only (no interpolation)") {
        test("empty template literal") {
            val code = "``"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    emptyList(),
                ),
            )
        }

        test("simple text") {
            val code = "`Hello, world!`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("Hello, world!"),
                    ),
                ),
            )
        }

        test("text with whitespace and punctuation") {
            val code = "`What a wonderful thing you are!`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("What a wonderful thing you are!"),
                    ),
                ),
            )
        }

        test("multiline text") {
            val code = "`line 1\nline 2\nline 3`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("line 1\nline 2\nline 3"),
                    ),
                ),
            )
        }
    }

    context("interpolation only (no text)") {
        test("single identifier") {
            val code = "`\${x}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        ExpressionPart(
                            Identifier("x"),
                        ),
                    ),
                ),
            )
        }

        test("literal value") {
            val code = "`\${42}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        ExpressionPart(
                            Number.SimpleNumber.IntegerNumber(42),
                        ),
                    ),
                ),
            )
        }

        test("arithmetic expression") {
            val code = "`\${1 + 2}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        ExpressionPart(
                            ArithmeticExpression.Addition(
                                Number.SimpleNumber.IntegerNumber(1),
                                Number.SimpleNumber.IntegerNumber(2),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("function call") {
            val code = "`\${getValue()}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        ExpressionPart(
                            FunctionCall(
                                Identifier("getValue"),
                                emptyList(),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("consecutive interpolations without text") {
            val code = "`\${x}\${y}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        ExpressionPart(
                            Identifier("x"),
                        ),
                        ExpressionPart(
                            Identifier("y"),
                        ),
                    ),
                ),
            )
        }
    }

    context("interpolation positions") {
        test("starts with an expression") {
            val code = "`\${name} is here`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        ExpressionPart(
                            Identifier("name"),
                        ),
                        StringPart(" is here"),
                    ),
                ),
            )
        }

        test("ends with an expression") {
            val code = "`Hello \${name}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("Hello "),
                        ExpressionPart(
                            Identifier("name"),
                        ),
                    ),
                ),
            )
        }

        test("expression in the middle") {
            val code = "`Hello \${name}, welcome!`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("Hello "),
                        ExpressionPart(
                            Identifier("name"),
                        ),
                        StringPart(", welcome!"),
                    ),
                ),
            )
        }

        test("multiple expressions in the middle and ends") {
            val code = "`prefix \${a} middle \${b} suffix`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("prefix "),
                        ExpressionPart(
                            Identifier("a"),
                        ),
                        StringPart(" middle "),
                        ExpressionPart(
                            Identifier("b"),
                        ),
                        StringPart(" suffix"),
                    ),
                ),
            )
        }
    }

    context("concatenation") {
        test("two template literals concatenated") {
            val code = "`Hello ` + `world!`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    TemplateLiteral(
                        listOf(
                            StringPart("Hello "),
                        ),
                    ),
                    TemplateLiteral(
                        listOf(
                            StringPart("world!"),
                        ),
                    ),
                ),
            )
        }

        test("two template literals with interpolations concatenated") {
            val code = "`Hello \${a}` + ` \${b}!`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    TemplateLiteral(
                        listOf(
                            StringPart("Hello "),
                            ExpressionPart(
                                Identifier("a"),
                            ),
                        ),
                    ),
                    TemplateLiteral(
                        listOf(
                            StringPart(" "),
                            ExpressionPart(
                                Identifier("b"),
                            ),
                            StringPart("!"),
                        ),
                    ),
                ),
            )
        }

        test("string literal and template literal concatenated") {
            val code = "\"Hello \" + `\${name}!`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    StringLiteral("Hello "),
                    TemplateLiteral(
                        listOf(
                            ExpressionPart(
                                Identifier("name"),
                            ),
                            StringPart("!"),
                        ),
                    ),
                ),
            )
        }

        test("template literal and string literal concatenated") {
            val code = "`Greetings \${title} ` + \"to the world\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    TemplateLiteral(
                        listOf(
                            StringPart("Greetings "),
                            ExpressionPart(
                                Identifier("title"),
                            ),
                            StringPart(" "),
                        ),
                    ),
                    StringLiteral("to the world"),
                ),
            )
        }
    }

    context("nested template literals") {
        test("template literal containing an expression with another template literal inside it") {
            val code = "`Outer \${`inner \${x}`}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("Outer "),
                        ExpressionPart(
                            TemplateLiteral(
                                listOf(
                                    StringPart("inner "),
                                    ExpressionPart(
                                        Identifier("x"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("template literal with nested template literal in ternary expression") {
            val code = "`Hello \${isFormal ? `Sir \${lastName}` : \"friend\"}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        StringPart("Hello "),
                        ExpressionPart(
                            TernaryExpression(
                                Identifier("isFormal"),
                                TemplateLiteral(
                                    listOf(
                                        StringPart("Sir "),
                                        ExpressionPart(
                                            Identifier("lastName"),
                                        ),
                                    ),
                                ),
                                StringLiteral("friend"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("template literal containing purely nested template literal") {
            val code = "`\${`nested`}`"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                TemplateLiteral(
                    listOf(
                        ExpressionPart(
                            TemplateLiteral(
                                listOf(
                                    StringPart("nested"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("usage in statements and structures") {
        test("in variable declaration") {
            val code = "const message = `Hello, \${name}!`;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Constant(
                    mapOf(
                        Identifier("message") to TemplateLiteral(
                            listOf(
                                StringPart("Hello, "),
                                ExpressionPart(
                                    Identifier("name"),
                                ),
                                StringPart("!"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("in return statement") {
            val code = "return `Result: \${value}`;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Return.Expression(
                    TemplateLiteral(
                        listOf(
                            StringPart("Result: "),
                            ExpressionPart(
                                Identifier("value"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("as function call argument") {
            val code = "console.log(`User: \${username}`);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionCall(
                    ChainedCall(
                        Identifier("console"),
                        Identifier("log"),
                    ),
                    listOf(
                        TemplateLiteral(
                            listOf(
                                StringPart("User: "),
                                ExpressionPart(
                                    Identifier("username"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("in array literal") {
            val code = "[`first`, `second: \${x}`]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArrayLiteral(
                    listOf(
                        TemplateLiteral(
                            listOf(
                                StringPart("first"),
                            ),
                        ),
                        TemplateLiteral(
                            listOf(
                                StringPart("second: "),
                                ExpressionPart(
                                    Identifier("x"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("in object literal") {
            val code = "{ greeting: `Hi, \${user}!` }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ObjectLiteral().withProperty(
                    Identifier("greeting"),
                    TemplateLiteral(
                        listOf(
                            StringPart("Hi, "),
                            ExpressionPart(
                                Identifier("user"),
                            ),
                            StringPart("!"),
                        ),
                    ),
                ),
            )
        }
    }
})
