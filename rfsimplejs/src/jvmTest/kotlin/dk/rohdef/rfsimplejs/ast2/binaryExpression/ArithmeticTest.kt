package dk.rohdef.rfsimplejs.ast2.binaryExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
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
                    Number.SimpleNumber.IntegerNumber(-9),
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

    xcontext("Subtraction") {}
    xcontext("Multiplication") {}
    xcontext("Division") {}
    xcontext("Modulo") {}

    xtest("Combination") {}
})