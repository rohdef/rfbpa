package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ParserSpec
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.parserContexts
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean

class ArrayTests : FunSpec({
    val testSamples = listOf(
        Number.SimpleNumber.IntegerNumber(42),
        AstBoolean.True,
        StringLiteral("\\\"hello\\\""),
        Identifier("_pinky4"),
        Null,
    )

    test("lit on") {
        val c = JavaScriptGrammar::class.createGrammar()
//        val c = Lg::class.createGrammar()
        val p = Parser(c)

        val result = p.run("[[true]]")
        result.stackTop shouldBe ArrayLiteral(
            ArrayLiteral(
                AstBoolean.True,
            ),
        )
    }

    parserContexts(
        ParserSpec(JavaScriptGrammar::class.createGrammar(), { Program(it) }, "Full JavaScript"),
        ParserSpec(LiteralGrammar::class.createGrammar(), { it }, "Declaration parser"),
    ) { parser, wrapping ->
        test("empty") {
            val result = parser.run("[]")

            result.stackTop shouldBe wrapping(
                ArrayLiteral(),
            )
        }

        test("single element array") {
            val result = parser.run("[42]")

            result.stackTop shouldBe wrapping(
                ArrayLiteral(Number.SimpleNumber.IntegerNumber(42)),
            )
        }

        test("single element array with spaces") {
            val result = parser.run("[ true ]")

            result.stackTop shouldBe wrapping(
                ArrayLiteral(AstBoolean.True),
            )
        }

        test("array") {
            val arrayParts = testSamples.map { it.toCode() }
                .joinToString(", ")
            val result = parser.run("[$arrayParts]")

            result.stackTop shouldBe wrapping(
                ArrayLiteral(testSamples),
            )
        }

        test("array with strange spacings") {
            val separators = listOf(
                "",
                "\n",
                " ",
                "\t",
                "\t\t",
                "   ",
                " \n\t ",
            )

            val arrayParts = testSamples.map { it.toCode() }
                .joinToString(",${separators.random()}")
            "[$arrayParts]".asClue {
                val result = parser.run(it)

                result.stackTop shouldBe wrapping(
                    ArrayLiteral(testSamples),
                )
            }
        }
    }
})