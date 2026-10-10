package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@DisplayName("Declarations")
class DeclarationTests : FunSpec({
    data class DeclarationData(
        val lvalue: Identifier,
        val expression: Expression,
    )

    val testSamples = listOf(
        DeclarationData(Identifier("foo"), Number.SimpleNumber.IntegerNumber(42.toBigInteger())),
        DeclarationData(Identifier("_b4r"), Boolean.True),
        DeclarationData(Identifier("\$yay"), StringLiteral("\\\"hello\\\"")),
        DeclarationData(Identifier("dInKy"), Identifier("_pinky4")),
        DeclarationData(Identifier("sad"), Null),
    )

    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)
    val wrapping: (Statement) -> Statement = { Program(it) }

    context("variable") {
        withTests(
            nameFn = { "var ${it.lvalue.toCode()}=${it.expression.toCode()}" },
            testSamples
        ) {
            val result = parser.run("var ${it.lvalue.toCode()}=${it.expression.toCode()}")

            result.stackTop shouldBe wrapping(
                Declaration.Variable(
                    mapOf(
                        it.lvalue to it.expression
                    ),
                ),
            )
        }

        withTests(
            nameFn = { "var ${it.lvalue.toCode()} = ${it.expression.toCode()}" },
            testSamples,
        ) {
            val result = parser.run("var ${it.lvalue.toCode()} = ${it.expression.toCode()}")

            result.stackTop shouldBe wrapping(
                Declaration.Variable(
                    mapOf(
                        it.lvalue to it.expression
                    ),
                ),
            )
        }

        withTests(
            nameFn = { "var\n${it.lvalue.toCode()} =\\n ${it.expression.toCode()}" },
            testSamples,
        ) {
            val result = parser.run("var ${it.lvalue.toCode()} =\n ${it.expression.toCode()}")

            result.stackTop shouldBe wrapping(
                Declaration.Variable(
                    mapOf(
                        it.lvalue to it.expression,
                    ),
                ),
            )
        }

        withTests(
            nameFn = { "var ${it.lvalue.toCode()} \\n= ${it.expression.toCode()}" },
            testSamples,
        ) {
            val result = parser.run("var${it.lvalue.toCode()} \n= ${it.expression.toCode()}")

            result.stackTop.shouldBeNull()
        }
    }

    context("let") {
        withTests(
            nameFn = { "let ${it.lvalue.toCode()} = ${it.expression.toCode()}" },
            testSamples,
        ) {
            val result = parser.run("let ${it.lvalue.toCode()} = ${it.expression.toCode()}")

            result.stackTop shouldBe wrapping(
                Declaration.Let(
                    mapOf(
                        it.lvalue to it.expression
                    ),
                ),
            )
        }

        test("simple multiple definition") {
            val expression = "let a=42,b=true,c=a,d=\"hey\""

            val result = parser.run(expression)

            result.stackTop shouldBe wrapping(
                Declaration.Let(
                    mapOf(
                        Identifier("a") to Number.SimpleNumber.IntegerNumber(42.toBigInteger()),
                        Identifier("b") to Boolean.True,
                        Identifier("c") to Identifier("a"),
                        Identifier("d") to StringLiteral("hey"),
                    ),
                ),
            )
        }

        val testSamples = listOf(
            DeclarationData(Identifier("foo"), Number.SimpleNumber.IntegerNumber(42.toBigInteger())),
            DeclarationData(Identifier("_b4r"), Boolean.True),
            DeclarationData(Identifier("\$yay"), StringLiteral("\\\"hello\\\"")),
            DeclarationData(Identifier("dInKy"), Identifier("_pinky4")),
            DeclarationData(Identifier("sad"), Null),
        )

        test("random probing of space rules") {
            val separators = listOf(
                "",
                "\n",
                " ",
                "\t",
                "\t\t",
                "   ",
                " \n\t ",
            )

            val assignments = testSamples
                .repeat(4)
                .map {
                    "${it.lvalue.toCode()} =${separators.random()}${it.expression.toCode()}"
                }
                .joinToString(",${separators.random()}")
            val expression = "let $assignments"

            val result = parser.run(expression)

            result.stackTop shouldBe wrapping(
                Declaration.Let(
                    mapOf(
                        Identifier("foo") to Number.SimpleNumber.IntegerNumber(42.toBigInteger()),
                        Identifier("_b4r") to Boolean.True,
                        Identifier("\$yay") to StringLiteral("\\\"hello\\\""),
                        Identifier("dInKy") to Identifier("_pinky4"),
                        Identifier("sad") to Null,

                        Identifier("foo") to Number.SimpleNumber.IntegerNumber(42.toBigInteger()),
                        Identifier("_b4r") to Boolean.True,
                        Identifier("\$yay") to StringLiteral("\\\"hello\\\""),
                        Identifier("dInKy") to Identifier("_pinky4"),
                        Identifier("sad") to Null,

                        Identifier("foo") to Number.SimpleNumber.IntegerNumber(42.toBigInteger()),
                        Identifier("_b4r") to Boolean.True,
                        Identifier("\$yay") to StringLiteral("\\\"hello\\\""),
                        Identifier("dInKy") to Identifier("_pinky4"),
                        Identifier("sad") to Null,

                        Identifier("foo") to Number.SimpleNumber.IntegerNumber(42.toBigInteger()),
                        Identifier("_b4r") to Boolean.True,
                        Identifier("\$yay") to StringLiteral("\\\"hello\\\""),
                        Identifier("dInKy") to Identifier("_pinky4"),
                        Identifier("sad") to Null,
                    ),
                ),
            )
        }
    }

    context("constant") {
        withTests(
            nameFn = { "const ${it.lvalue.toCode()} = ${it.expression.toCode()}" },
            testSamples,
        ) {
            val result = parser.run("const ${it.lvalue.toCode()} = ${it.expression.toCode()}")

            result.stackTop shouldBe wrapping(
                Declaration.Constant(
                    mapOf(
                        it.lvalue to it.expression
                    ),
                ),
            )
        }
    }
})

fun <T> List<T>.repeat(times: Int): List<T> {
    require(times >= 0) { "times must be non-negative" }
    return buildList(size * times) {
        repeat(times) {
            addAll(this@repeat)
        }
    }
}