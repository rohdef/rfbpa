package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import dk.rohdef.rfsimplejs.ParserSpec
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.DeclarationGrammar
import dk.rohdef.rfsimplejs.parserContexts
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class DeclarationTests : FunSpec({
    data class DeclarationData(
        val lvalue: Identifier,
        val expression: Expression,
    )

    val testSamples = listOf(
        // TODO find out what to do with just a lvalue
//        "a",
        DeclarationData(Identifier("foo"), Number.SimpleNumber.IntegerNumber("42".toBigInteger())),
        DeclarationData(Identifier("_b4r"), Boolean.True),
        DeclarationData(Identifier("\$yay"), StringLiteral("\\\"hello\\\"")),
        DeclarationData(Identifier("dInKy"), Identifier("_pinky4")),
        DeclarationData(Identifier("sad"), Null),
    )

    parserContexts(
        ParserSpec(JavaScriptGrammar::class.createGrammar(), { Program(it) }, "Full JavaScript"),
        ParserSpec(DeclarationGrammar::class.createGrammar(), { it }, "Declaration parser"),
    ) { parser, wrapping ->
        context("variables") {
            withTests(
                nameFn = { "var ${it.lvalue.toCode()}=${it.expression.toCode()}" },
                testSamples
            ) {
                val result = parser.run("var ${it.lvalue.toCode()}=${it.expression.toCode()}")

                result.stackTop shouldBe wrapping(
                    Declaration.Variable(it.lvalue, it.expression)
                )
            }

            withTests(
                nameFn = { "var ${it.lvalue.toCode()} = ${it.expression.toCode()}" },
                testSamples
            ) {
                val result = parser.run("var ${it.lvalue.toCode()} = ${it.expression.toCode()}")

                result.stackTop shouldBe wrapping(
                    Declaration.Variable(it.lvalue, it.expression)
                )
            }

            withTests(
                nameFn = { "var\n${it.lvalue.toCode()} = ${it.expression.toCode()}" },
                testSamples,
            ) {
                val result = parser.run("var\n${it.lvalue.toCode()} = ${it.expression.toCode()}")

                result.stackTop shouldBe wrapping(
                    Declaration.Variable(it.lvalue, it.expression)
                )
            }

            withTests(
                nameFn = { "var\n${it.lvalue.toCode()} =\n ${it.expression.toCode()}" },
                testSamples,
            ) {
                val result = parser.run("var ${it.lvalue.toCode()} =\n ${it.expression.toCode()}")

                result.stackTop shouldBe wrapping(
                    Declaration.Variable(it.lvalue, it.expression)
                )
            }

            withTests(
                nameFn = { "var ${it.lvalue.toCode()} \n= ${it.expression.toCode()}" },
                testSamples,
            ) {
                val result = parser.run("var${it.lvalue.toCode()} \n= ${it.expression.toCode()}")

                result.stackTop.shouldBeNull()
            }
        }

        context("lets") {
            withTests(
                nameFn = { "let ${it.lvalue.toCode()} = ${it.expression.toCode()}" },
                testSamples
            ) {
                val result = parser.run("let ${it.lvalue.toCode()} = ${it.expression.toCode()}")

                result.stackTop shouldBe wrapping(
                    Declaration.Let(it.lvalue, it.expression)
                )
            }
        }

        context("constants") {
            withTests(
                nameFn = { "const ${it.lvalue.toCode()} = ${it.expression.toCode()}" },
                testSamples
            ) {
                val result = parser.run("const ${it.lvalue.toCode()} = ${it.expression.toCode()}")

                result.stackTop shouldBe wrapping(
                    Declaration.Constant(it.lvalue, it.expression)
                )
            }
        }
    }
})