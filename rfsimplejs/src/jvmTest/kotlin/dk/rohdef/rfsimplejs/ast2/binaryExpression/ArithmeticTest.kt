package dk.rohdef.rfsimplejs.ast2.binaryExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ArithmeticTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("Addition") {
        test("should add two numbers") {
            val code = "1 + 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should add boolean and string") {
            val code = "true + \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should add array and object") {
            val code = "[]+{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should add with negative number") {
            val code = "3+-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("should add with odd whitespaces") {
            val code = "29.5   \t+   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("Subtraction") {
        test("should subtract two numbers") {
            val code = "1 - 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should subtract boolean and string") {
            val code = "true - \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should subtract array and object") {
            val code = "[]-{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should subtract with negative number") {
            val code = "3--9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("should subtract with odd whitespaces") {
            val code = "29.5   \t-   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("Multiplication") {
        test("should multiply two numbers") {
            val code = "1 * 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should multiply boolean and string") {
            val code = "true * \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should multiply array and object") {
            val code = "[]*{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should multiply with negative number") {
            val code = "3*-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("should multiply with odd whitespaces") {
            val code = "29.5   \t*   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("Division") {
        test("should divide two numbers") {
            val code = "1 / 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should divide boolean and string") {
            val code = "true / \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should divide array and object") {
            val code = "[]/{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should divide with negative number") {
            val code = "3/-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("should divide with odd whitespaces") {
            val code = "29.5   \t/   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("Modulo") {
        test("should modulo two numbers") {
            val code = "1 % 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should modulo boolean and string") {
            val code = "true % \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should modulo array and object") {
            val code = "[]%{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should modulo with negative number") {
            val code = "3%-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("should modulo with odd whitespaces") {
            val code = "29.5   \t%   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    xtest("Combination") {}
})