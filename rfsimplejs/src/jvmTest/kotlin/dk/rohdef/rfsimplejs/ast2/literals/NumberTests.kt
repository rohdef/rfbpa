package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Number literals")
class NumberTests : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)
    val wrapping: (Statement) -> Statement = { Program(it) }

    context("numbers") {
            context("integers") {
                test("positive") {
                    val result = parser.run("57005")

                    result.stackTop shouldBe wrapping(
                        Number.SimpleNumber.IntegerNumber(57005.toBigInteger())
                    )
                }

                test("negative") {
                    val result = parser.run("-8")

                    result.stackTop shouldBe wrapping(
                        PrefixExpression.Minus(
                            Number.SimpleNumber.IntegerNumber(8.toBigInteger())
                        )
                    )
                }

                test("positive with prefix plus") {
                    val result = parser.run("+19")

                    result.stackTop shouldBe wrapping(
                        PrefixExpression.Plus(
                            Number.SimpleNumber.IntegerNumber(19.toBigInteger())
                        )
                    )
                }
            }
        }

        context("floating points") {
            test("positive") {
                val result = parser.run("318.445")

                result.stackTop shouldBe wrapping(
                    Number.SimpleNumber.DecimalNumber("318.445".toBigDecimal())
                )
            }

            test("positive starting with a dot") {
                val result = parser.run(".43")

                result.stackTop shouldBe wrapping(
                    Number.SimpleNumber.DecimalNumber(".43".toBigDecimal())
                )
            }

            test("negative") {
                val result = parser.run("-29.543")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Minus(
                        Number.SimpleNumber.DecimalNumber("29.543".toBigDecimal())
                    )
                )
            }

            test("negative starting with a dot") {
                val result = parser.run("-.621")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Minus(
                        Number.SimpleNumber.DecimalNumber(".621".toBigDecimal())
                    )
                )
            }

            test("positive with prefix plus") {
                val result = parser.run("+3.1415")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Plus(
                        Number.SimpleNumber.DecimalNumber("3.1415".toBigDecimal())
                    )
                )
            }

            test("positive starting with a dot and a prefix plus") {
                val result = parser.run("+.69")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Plus(
                        Number.SimpleNumber.DecimalNumber(".69".toBigDecimal())
                    )
                )
            }
        }

        context("hexadecimal") {
            test("positive") {
                val result = parser.run("0xFF342500000000")

                result.stackTop shouldBe wrapping(
                    Number.HexadecimalNumber("0xFF342500000000")
                )
            }

            test("negative") {
                val result = parser.run("-0x41DEad")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Minus(
                        Number.HexadecimalNumber("0x41DEad")
                    )
                )
            }

            test("positive with a prefix plus") {
                val result = parser.run("+0xCafeBabe")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Plus(
                        Number.HexadecimalNumber("0xCafeBabe")
                    )
                )
            }
        }

        context("binary") {
            test("positive") {
                val result = parser.run("0b1001")

                result.stackTop shouldBe wrapping(
                    Number.BinaryNumber("0b1001")
                )
            }

            test("negative") {
                val result = parser.run("-0b0")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Minus(
                        Number.BinaryNumber("0b0")
                    )
                )
            }

            test("positive with a prefix plus") {
                val result = parser.run("+0b1111000010100101")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Plus(
                        Number.BinaryNumber("0b1111000010100101")
                    )
                )
            }
        }

        context("scientific notation") {
            test("positive") {
                val result = parser.run("3e0")

                result.stackTop shouldBe wrapping(
                    Number.ScientificNumber(
                        Number.SimpleNumber.IntegerNumber(3.toBigInteger()),
                        Number.SimpleNumber.IntegerNumber(0.toBigInteger()),
                    )
                )
            }

            test("simple decimal coefficient") {
                val result = parser.run(".31e3")

                result.stackTop shouldBe wrapping(
                    Number.ScientificNumber(
                        Number.SimpleNumber.DecimalNumber(".31".toBigDecimal()),
                        Number.SimpleNumber.IntegerNumber(3.toBigInteger()),
                    )
                )
            }

            test("simple decimal with negative exponent") {
                val result = parser.run("4.2e-5")

                result.stackTop shouldBe wrapping(
                    Number.ScientificNumber(
                        Number.SimpleNumber.DecimalNumber("4.2".toBigDecimal()),
                        Number.SimpleNumber.IntegerNumber((-5).toBigInteger()),
                    )
                )
            }

            test("simple decimal starting with dot with negative exponent") {
                val result = parser.run(".4e-5")

                result.stackTop shouldBe wrapping(
                    Number.ScientificNumber(
                        Number.SimpleNumber.DecimalNumber(".4".toBigDecimal()),
                        Number.SimpleNumber.IntegerNumber((-5).toBigInteger()),
                    )
                )
            }

            test("positive with prefix plus") {
                val result = parser.run("+9.1e44")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Plus(
                        Number.ScientificNumber(
                            Number.SimpleNumber.DecimalNumber("9.1".toBigDecimal()),
                            Number.SimpleNumber.IntegerNumber(44.toBigInteger()),
                        )
                    )
                )
            }

            test("positive with a prefix plus and negative exponent") {
                val result = parser.run("+52e-5")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Plus(
                        Number.ScientificNumber(
                            Number.SimpleNumber.IntegerNumber(52.toBigInteger()),
                            Number.SimpleNumber.IntegerNumber((-5).toBigInteger()),
                        )
                    )
                )
            }

            test("positive decimal starting with a dot with plus and negative exponent") {
                val result = parser.run("+.79e-5")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Plus(
                        Number.ScientificNumber(
                            Number.SimpleNumber.DecimalNumber(".79".toBigDecimal()),
                            Number.SimpleNumber.IntegerNumber((-5).toBigInteger()),
                        )
                    )
                )
            }

            test("negative") {
                val result = parser.run("-66e27")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Minus(
                        Number.ScientificNumber(
                            Number.SimpleNumber.IntegerNumber(66.toBigInteger()),
                            Number.SimpleNumber.IntegerNumber(27.toBigInteger()),
                        )
                    )
                )
            }

            test("negative decimal starting with a dot") {
                val result = parser.run("-.31e3")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Minus(
                        Number.ScientificNumber(
                            Number.SimpleNumber.DecimalNumber(".31".toBigDecimal()),
                            Number.SimpleNumber.IntegerNumber(3.toBigInteger()),
                        )
                    )
                )
            }

            test("negative with a negative exponent") {
                val result = parser.run("-52e-5")

                result.stackTop shouldBe wrapping(
                    PrefixExpression.Minus(
                        Number.ScientificNumber(
                            Number.SimpleNumber.IntegerNumber(52.toBigInteger()),
                            Number.SimpleNumber.IntegerNumber((-5).toBigInteger()),
                        )
                    )
                )
            }
        }
})