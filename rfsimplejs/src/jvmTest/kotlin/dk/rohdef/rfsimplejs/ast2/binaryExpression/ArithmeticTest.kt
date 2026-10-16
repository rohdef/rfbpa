package dk.rohdef.rfsimplejs.ast2.binaryExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.ChainedCall
import dk.rohdef.rfsimplejs.ast2.statement.EnclosedExpression
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.*
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Arithmetic")
class ArithmeticTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("addition") {
        test("of two numbers") {
            val code = "1 + 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true + \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]+{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3+-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t+   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "2 + 3 + 4;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    ArithmeticExpression.Addition(
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                    Number.SimpleNumber.IntegerNumber(4),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() + baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    ChainedCall(
                        Identifier("foo"),
                        FunctionCall(Identifier("bar")),
                    ),
                    ChainedCall(
                        ArrayReference(
                            Identifier("baz"),
                            Number.SimpleNumber.IntegerNumber(0),
                        ),
                        Identifier("qux"),
                    ),
                ),
            )
        }
    }

    context("subtraction") {
        test("of two numbers") {
            val code = "1 - 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true - \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]-{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3--9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t-   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "2 - 3 - 4;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Subtraction(
                    ArithmeticExpression.Subtraction(
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                    Number.SimpleNumber.IntegerNumber(4),
                ),
            )
        }
    }

    context("multiplication") {
        test("of two numbers") {
            val code = "1 * 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true * \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]*{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("of with negative number") {
            val code = "3*-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t*   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the right") {
            val code = "2 * 3 * 4;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    Number.SimpleNumber.IntegerNumber(2),
                    ArithmeticExpression.Multiplication(
                        Number.SimpleNumber.IntegerNumber(3),
                        Number.SimpleNumber.IntegerNumber(4),
                    ),
                ),
            )
        }
    }

    context("division") {
        test("of two numbers") {
            val code = "1 / 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true / \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]/{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("of negative number") {
            val code = "3/-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t/   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the right") {
            val code = "8/4/2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    Number.SimpleNumber.IntegerNumber(8),
                    ArithmeticExpression.Division(
                        Number.SimpleNumber.IntegerNumber(4),
                        Number.SimpleNumber.IntegerNumber(2),
                    ),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() / baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Division(
                    ChainedCall(
                        Identifier("foo"),
                        FunctionCall(Identifier("bar")),
                    ),
                    ChainedCall(
                        ArrayReference(
                            Identifier("baz"),
                            Number.SimpleNumber.IntegerNumber(0),
                        ),
                        Identifier("qux"),
                    ),
                ),
            )
        }
    }

    context("modulo") {
        test("of two numbers") {
            val code = "1 % 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true % \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]%{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("of with negative number") {
            val code = "3%-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t%   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the right") {
            val code = "8%3%2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Modulo(
                    Number.SimpleNumber.IntegerNumber(8),
                    ArithmeticExpression.Modulo(
                        Number.SimpleNumber.IntegerNumber(3),
                        Number.SimpleNumber.IntegerNumber(2),
                    ),
                ),
            )
        }
    }

    context("combinations") {
        test("operator precedence in the middle") {
            val code = "1 + 2 * 3 + 4"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    ArithmeticExpression.Addition(
                        Number.SimpleNumber.IntegerNumber(1),
                        ArithmeticExpression.Multiplication(
                            Number.SimpleNumber.IntegerNumber(2),
                            Number.SimpleNumber.IntegerNumber(3),
                        ),
                    ),
                    Number.SimpleNumber.IntegerNumber(4),
                ),
            )
        }

        test("operator precedence to the left") {
            val code = "1/3+4+5"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    ArithmeticExpression.Addition(
                        ArithmeticExpression.Division(
                            Number.SimpleNumber.IntegerNumber(1),
                            Number.SimpleNumber.IntegerNumber(3),
                        ),
                        Number.SimpleNumber.IntegerNumber(4),
                    ),
                    Number.SimpleNumber.IntegerNumber(5),
                ),
            )
        }

        test("operator precedence to the right") {
            val code = "3+4+13%7"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Addition(
                    ArithmeticExpression.Addition(
                        Number.SimpleNumber.IntegerNumber(3),
                        Number.SimpleNumber.IntegerNumber(4),
                    ),
                    ArithmeticExpression.Modulo(
                        Number.SimpleNumber.IntegerNumber(13),
                        Number.SimpleNumber.IntegerNumber(7),
                    ),
                ),
            )
        }

        test("parenthesis enclosed and precedence") {
            val code = "( 1 + 2 * 9 ) * ( 3 + 4 - 5 )"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArithmeticExpression.Multiplication(
                    EnclosedExpression(
                        ArithmeticExpression.Addition(
                            Number.SimpleNumber.IntegerNumber(1),
                            ArithmeticExpression.Multiplication(
                                Number.SimpleNumber.IntegerNumber(2),
                                Number.SimpleNumber.IntegerNumber(9),
                            ),
                        ),
                    ),
                    EnclosedExpression(
                        ArithmeticExpression.Subtraction(
                            ArithmeticExpression.Addition(
                                Number.SimpleNumber.IntegerNumber(3),
                                Number.SimpleNumber.IntegerNumber(4),
                            ),
                            Number.SimpleNumber.IntegerNumber(5)
                        ),
                    ),
                ),
            )
        }
    }
})