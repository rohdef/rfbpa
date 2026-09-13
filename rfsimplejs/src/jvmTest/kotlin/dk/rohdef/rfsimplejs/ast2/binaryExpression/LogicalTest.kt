package dk.rohdef.rfsimplejs.ast2.binaryExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LogicalTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("Equals (==)") {
        test("should compare two numbers") {
            val code = "1 == 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.Equals(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should compare boolean and string") {
            val code = "true == \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.Equals(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should compare array and object") {
            val code = "[]=={}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.Equals(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should compare with negative number") {
            val code = "3==-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.Equals(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("should compare with odd whitespaces") {
            val code = "29.5   \t==   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.Equals(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("NonCoercingEquals (===)") {
        test("should strictly compare two numbers") {
            val code = "1 === 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.NonCoercingEquals(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should strictly compare boolean and string") {
            val code = "true === \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.NonCoercingEquals(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should strictly compare array and object") {
            val code = "[]==={}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.NonCoercingEquals(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should strictly compare with negative number") {
            val code = "3===-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.NonCoercingEquals(
                    Number.SimpleNumber.IntegerNumber(3),
                    Number.SimpleNumber.IntegerNumber(-9),
                )
            )
        }

        test("should strictly compare with odd whitespaces") {
            val code = "29.5   \t===   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.NonCoercingEquals(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("NotEquals (!=)") {
        test("should compare inequality of two numbers") {
            val code = "1 != 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        Number.SimpleNumber.IntegerNumber(1),
                        Number.SimpleNumber.IntegerNumber(2),
                    )
                )
            )
        }

        test("should compare inequality of boolean and string") {
            val code = "true != \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        Boolean.True,
                        StringLiteral("false"),
                    )
                )
            )
        }

        test("should compare inequality of array and object") {
            val code = "[]!={}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        ArrayLiteral(),
                        ObjectLiteral(),
                    )
                )
            )
        }

        test("should compare inequality with negative number") {
            val code = "3!=-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        Number.SimpleNumber.IntegerNumber(3),
                        Number.SimpleNumber.IntegerNumber(-9),
                    )
                )
            )
        }

        test("should compare inequality with odd whitespaces") {
            val code = "29.5   \t!=   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                        Number.SimpleNumber.IntegerNumber(19),
                    )
                )
            )
        }
    }

    context("NonCoercingNotEquals (!==)") {
        test("should strictly compare inequality of two numbers") {
            val code = "1 !== 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.NonCoercingEquals(
                        Number.SimpleNumber.IntegerNumber(1),
                        Number.SimpleNumber.IntegerNumber(2),
                    )
                )
            )
        }

        test("should strictly compare inequality of boolean and string") {
            val code = "true !== \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.NonCoercingEquals(
                        Boolean.True,
                        StringLiteral("false"),
                    )
                )
            )
        }

        test("should strictly compare inequality of array and object") {
            val code = "[]!=={}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.NonCoercingEquals(
                        ArrayLiteral(),
                        ObjectLiteral(),
                    )
                )
            )
        }

        test("should strictly compare inequality with negative number") {
            val code = "3!== -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.NonCoercingEquals(
                        Number.SimpleNumber.IntegerNumber(3),
                        Number.SimpleNumber.IntegerNumber(-9),
                    )
                )
            )
        }

        test("should strictly compare inequality with odd whitespaces") {
            val code = "29.5   \t!==   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.NonCoercingEquals(
                        Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                        Number.SimpleNumber.IntegerNumber(19),
                    )
                )
            )
        }
    }

    context("LogicalOr (||)") {
        test("should logical or two numbers") {
            val code = "1 || 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalOr(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should logical or boolean and string") {
            val code = "true || \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalOr(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should logical or array and object") {
            val code = "[]||{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalOr(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should logical or with negative number") {
            val code = "3||-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalOr(
                    Number.SimpleNumber.IntegerNumber(3),
                    Number.SimpleNumber.IntegerNumber(-9),
                )
            )
        }

        test("should logical or with odd whitespaces") {
            val code = "29.5   \t||   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalOr(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("LogicalAnd (&&)") {
        test("should logical and two numbers") {
            val code = "1 && 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalAnd(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("should logical and boolean and string") {
            val code = "true && \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalAnd(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("should logical and array and object") {
            val code = "[]&&{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalAnd(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("should logical and with negative number") {
            val code = "3&&-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalAnd(
                    Number.SimpleNumber.IntegerNumber(3),
                    Number.SimpleNumber.IntegerNumber(-9),
                )
            )
        }

        test("should logical and with odd whitespaces") {
            val code = "29.5   \t&&   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalAnd(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }
})
