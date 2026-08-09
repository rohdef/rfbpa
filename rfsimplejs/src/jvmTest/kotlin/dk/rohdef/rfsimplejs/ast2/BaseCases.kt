package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.literals.Number
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

class BaseCases : FunSpec({
    val grammar = SimpleJavaScript2::class.createGrammar()
    val parser = Parser(grammar)

    context("whitespace programs") {
        test("empty string") {
            val text = ""

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("spaces") {
            val text = "    "

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("tabs") {
            val text = "\t\t"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("new lines") {
            val text = "\n\n\n"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program()
        }

        test("mix") {
            val text = "\n\n   \n\t\t\n\r\n\r\r\n\n"

            val result = parser.run(text)

            result.stackTop shouldBe Program()
        }
    }

    context("literals") {
        context("numbers") {
            context("integers") {
                withTests(
                    "57005",
                    "-8",
                    "+19",
                ) {
                    val result = parser.run(it)

                    result.stackTop shouldBe Program(
                        Number.SimpleNumber.IntegerNumber(it.toBigInteger())
                    )
                }
            }

            test("simple integer with semicolon") {
                val text = "57005;"

                val result = parser.run(text)

                result.stackTop shouldBe Program(
                    Number.SimpleNumber.IntegerNumber("57005".toBigInteger())
                )
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

                result.stackTop shouldBe Program(
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

                result.stackTop shouldBe Program(
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

                result.stackTop shouldBe Program(
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

                result.stackTop shouldBe Program(
                    it.toScientific(),
                )
            }
        }
    }
})