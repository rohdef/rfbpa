package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import dk.rohdef.rfsimplejs.ParserSpec
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.grammar.literal.NumberGrammar
import dk.rohdef.rfsimplejs.parserContexts
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

class NumberTests : FunSpec({
    parserContexts(
        ParserSpec(JavaScriptGrammar::class.createGrammar(), { Program(it) }, "Full JavaScript"),
        ParserSpec(LiteralGrammar::class.createGrammar(), { it }, "Literal parser"),
        ParserSpec(NumberGrammar::class.createGrammar(), { it }, "Number parser"),
    ) { parser, wrapping ->
        context("numbers") {
            context("integer s") {
                withTests(
                    "57005",
                    "-8",
                    "+19",
                ) {
                    val result = parser.run(it)

                    result.stackTop shouldBe wrapping(
                        Number.SimpleNumber.IntegerNumber(it.toBigInteger())
                    )
                }
            }
        }

        context("floating point") {
            withTests(
                "318.445",
                "-29.543",
                "+3.1415",
                ".43",
                "-.621",
                "+.69",
            ) {
                val result = parser.run(it)

                result.stackTop shouldBe wrapping(
                    Number.SimpleNumber.DecimalNumber(it.toBigDecimal())
                )
            }
        }

        context("hexadecimal") {
            withTests(
                "0xFF342500000000",
                "+0xCafeBabe",
                "-0x41DEad",
            ) {
                val result = parser.run(it)

                result.stackTop shouldBe wrapping(
                    Number.HexadecimalNumber(it)
                )
            }
        }

        context("binary") {
            withTests(
                "0b1001",
                "-0b0",
                "+0b1111000010100101",
            ) {
                val result = parser.run(it)

                result.stackTop shouldBe wrapping(
                    Number.BinaryNumber(it)
                )
            }
        }

        context("scientific notation") {
            data class ScientificTest(
                val input: String,
                val coefficient: Number.SimpleNumber,
                val exponent: Number.SimpleNumber.IntegerNumber,
            ) {
                constructor(
                    input: String,
                    coefficient: Int,
                    exponent: Int,
                ) : this(
                    input,
                    Number.SimpleNumber.IntegerNumber(coefficient.toBigInteger()),
                    Number.SimpleNumber.IntegerNumber(exponent.toBigInteger()),
                )

                constructor(
                    input: String,
                    coefficient: Double,
                    exponent: Int,
                ) : this(
                    input,
                    Number.SimpleNumber.DecimalNumber(coefficient.toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(exponent.toBigInteger()),
                )

                fun toScientific() = Number.ScientificNumber(
                    coefficient,
                    exponent,
                )
            }

            withTests(
                nameFn = { "${it.input} -> coefficient: ${it.coefficient.number}, exponent: ${it.exponent.number}" },
                ScientificTest("3e0", 3, 0),
                ScientificTest("+9.1e44", 9.1, 44),
                ScientificTest("-66e27", -66, 27),
                ScientificTest(".31e3", .31, 3),
                ScientificTest("-.31e3", -.31, 3),
                ScientificTest("4.2e-5", 4.2, -5),
                ScientificTest("+52e-5", 52, -5),
                ScientificTest(".4e-5", .4, -5),
                ScientificTest("-52e-5", -52, -5),
                ScientificTest("+.79e-5", .79, -5),
            ) {
                val result = parser.run(it.input)

                result.stackTop shouldBe wrapping(
                    it.toScientific(),
                )
            }
        }
    }
})