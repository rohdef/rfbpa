package dk.rohdef.rfsimplejs.ast2.binaryExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
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

    context("Equals `==`") {
        test("comparing two numbers") {
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

        test("comparing boolean and string") {
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

        test("comparing array and object") {
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

        test("comparing with negative number") {
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

        test("with odd whitespaces") {
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

        test("associates to the left") {
            val code = "a == b == c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.Equals(
                    LogicExpression.Equals(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    Identifier("c"),
                )
            )
        }
    }

    context("NonCoercingEquals `===`") {
        test("comparing two numbers") {
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

        test("comparing boolean and string") {
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

        test("comparing array and object") {
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

        test("comparing with negative number") {
            val code = "3===-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.NonCoercingEquals(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("with odd whitespaces") {
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

        test("associates to the left") {
            val code = "a === b === c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.NonCoercingEquals(
                    LogicExpression.NonCoercingEquals(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    Identifier("c"),
                )
            )
        }
    }

    context("NotEquals `!=`") {
        test("comparing two numbers") {
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

        test("comparing boolean and string") {
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

        test("comparing array and object") {
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

        test("comparing with negative number") {
            val code = "3!=-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        Number.SimpleNumber.IntegerNumber(3),
                        PrefixExpression.Minus(
                            Number.SimpleNumber.IntegerNumber(9),
                        ),
                    )
                )
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t!=   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                        Number.SimpleNumber.IntegerNumber(19),
                    ),
                ),
            )
        }

        test("associates to the left") {
            val code = "a != b != c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.Equals(
                        PrefixExpression.Not(
                            LogicExpression.Equals(
                                Identifier("a"),
                                Identifier("b"),
                            ),
                        ),
                        Identifier("c"),
                    ),
                ),
            )
        }
    }

    context("NonCoercingNotEquals `!==`") {
        test("comparing two numbers") {
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

        test("comparing boolean and string") {
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

        test("comparing array and object") {
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

        test("comparing negative number") {
            val code = "3!== -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.NonCoercingEquals(
                        Number.SimpleNumber.IntegerNumber(3),
                        PrefixExpression.Minus(
                            Number.SimpleNumber.IntegerNumber(9),
                        ),
                    )
                )
            )
        }

        test("with odd whitespaces") {
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

        test("associates to the left") {
            val code = "a !== b !== c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    LogicExpression.NonCoercingEquals(
                        PrefixExpression.Not(
                            LogicExpression.NonCoercingEquals(
                                Identifier("a"),
                                Identifier("b"),
                            ),
                        ),
                        Identifier("c"),
                    ),
                ),
            )
        }
    }

    context("LogicalOr (||)") {
        test("comparing two numbers") {
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

        test("comparing boolean and string") {
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

        test("comparing array and object") {
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

        test("comparing negative number") {
            val code = "3||-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalOr(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("with odd whitespaces") {
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

        test("associates to the left") {
            val code = "a || b || c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalOr(
                    LogicExpression.LogicalOr(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    Identifier("c"),
                ),
            )
        }
    }

    context("LogicalAnd (&&)") {
        test("comparing two numbers") {
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

        test("comparing boolean and string") {
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

        test("comparing array and object") {
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

        test("comparing negative number") {
            val code = "3&&-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalAnd(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("with odd whitespaces") {
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

        test("associates to the left") {
            val code = "a && b && c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LogicalAnd(
                    LogicExpression.LogicalAnd(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    Identifier("c"),
                ),
            )
        }
    }

    context("LessThan (<)") {
        test("comparing two numbers") {
            val code = "1 < 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThan(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("comparing boolean and string") {
            val code = "true < \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThan(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("comparing array and object") {
            val code = "[]<{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThan(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("comparing with negative number") {
            val code = "3<-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThan(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t<   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThan(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("GreaterThan (>)") {
        test("comparing two numbers") {
            val code = "1 > 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThan(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("comparing boolean and string") {
            val code = "true > \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThan(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("comparing array and object") {
            val code = "[]>{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThan(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("comparing negative number") {
            val code = "3>-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThan(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t>   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThan(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("LessThanOrEqual (<=)") {
        test("comparing two numbers") {
            val code = "1 <= 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThanOrEqual(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("comparing boolean and string") {
            val code = "true <= \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThanOrEqual(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("comparing array and object") {
            val code = "[]<={}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThanOrEqual(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("comparing negative number") {
            val code = "3<=-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThanOrEqual(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t<=   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.LessThanOrEqual(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }

    context("GreaterThanOrEqual (>=)") {
        test("comparing two numbers") {
            val code = "1 >= 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThanOrEqual(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                )
            )
        }

        test("comparing boolean and string") {
            val code = "true >= \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThanOrEqual(
                    Boolean.True,
                    StringLiteral("false"),
                )
            )
        }

        test("comparing array and object") {
            val code = "[]>={}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThanOrEqual(
                    ArrayLiteral(),
                    ObjectLiteral(),
                )
            )
        }

        test("comparing negative number") {
            val code = "3>=-9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThanOrEqual(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                )
            )
        }

        test("with odd whitespaces") {
            val code = "29.5   \t>=   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                LogicExpression.GreaterThanOrEqual(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                    Number.SimpleNumber.IntegerNumber(19),
                )
            )
        }
    }
})