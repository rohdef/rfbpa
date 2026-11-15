package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ParserSpec
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.TemplateLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.TemplateLiteral.TemplateLiteralPart.StringPart
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.grammar.literal.StringGrammar
import dk.rohdef.rfsimplejs.parserContexts
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

@DisplayName("String literals")
class StringTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    parserContexts(
        ParserSpec(JavaScriptGrammar::class.createGrammar(), { Program(it) }, "Full JavaScript"),
        ParserSpec(LiteralGrammar::class.createGrammar(), { it }, "Literal parser"),
        ParserSpec(StringGrammar::class.createGrammar(), { it }, "String parser"),
    ) { parser, wrapping ->
        withTests(
            """
                ""
            """.trimIndent(),
            """
                "Hello, world!"
            """.trimIndent(),
            """
                "\"Hello, world!\""
            """.trimIndent(),
            """
                'What a wonderful thing you are'
            """.trimIndent(),
            """
                "The song 'Peace somehow'"
            """.trimIndent(),
            """
                "A quote looks like: \" and escape should work"
            """.trimIndent(),
            """
                'Single quote : \' like this'
            """.trimIndent(),
        ) {
            val result = parser.run(it)

            val stringWithoutQuotes = it.substring(1, it.length - 1)
            result.stackTop shouldBe wrapping(
                StringLiteral(stringWithoutQuotes)
            )
        }
    }

    context("multiline concatenation") {
        context("double quote string") {
            test("+ on same line") {
                val code = """
                    "Hello, " +
                    "world!"
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    ArithmeticExpression.Addition(
                        StringLiteral("Hello, "),
                        StringLiteral("world!"),
                    ),
                )
            }

            test("+ on next line") {
                val code = """
                    "Hello, "
                    + "world!"
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    ArithmeticExpression.Addition(
                        StringLiteral("Hello, "),
                        StringLiteral("world!"),
                    ),
                )
            }
        }

        context("single quote string") {
            test("+ on same line") {
                val code = """
                    'Hello, ' +
                    'world!'
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    ArithmeticExpression.Addition(
                        StringLiteral("Hello, "),
                        StringLiteral("world!"),
                    ),
                )
            }

            test("+ on next line") {
                val code = """
                    'Hello, '
                    + 'world!'
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    ArithmeticExpression.Addition(
                        StringLiteral("Hello, "),
                        StringLiteral("world!"),
                    ),
                )
            }
        }

        context("template literal") {
            test("+ on same line") {
                val code = """
                    `Hello, ` +
                    `world!`
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    ArithmeticExpression.Addition(
                        TemplateLiteral(StringPart("Hello, ")),
                        TemplateLiteral(StringPart("world!")),
                    ),
                )
            }

            test("+ on next line") {
                val code = """
                    `Hello, `
                    + `world!`
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    ArithmeticExpression.Addition(
                        TemplateLiteral(StringPart("Hello, ")),
                        TemplateLiteral(StringPart("world!")),
                    ),
                )
            }
        }

        context("mixed string types") {
            test("+ on same line and next line across multiple lines") {
                val code = """
                    "First " +
                    'second '
                    + `third`
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    ArithmeticExpression.Addition(
                        ArithmeticExpression.Addition(
                            StringLiteral("First "),
                            StringLiteral("second "),
                        ),
                        TemplateLiteral(StringPart("third")),
                    ),
                )
            }
        }
    }
})