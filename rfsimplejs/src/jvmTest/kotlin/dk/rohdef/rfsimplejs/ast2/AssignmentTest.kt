package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.Assignment
import dk.rohdef.rfsimplejs.ast2.statement.ChainedCall
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number as AstNumber

@DisplayName("Assignments")
class AssignmentTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("simple identifier assignments") {
        test("simple number assignment") {
            val code = "a = 1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("a"),
                    AstNumber.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("simple null assignment") {
            val code = "item = null"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("item"),
                    Null,
                ),
            )
        }

        test("assign identifier to identifier") {
            val code = "a = b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("a"),
                    Identifier("b"),
                ),
            )
        }

        test("assign expression result") {
            val code = "x = 1 + 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("x"),
                    ArithmeticExpression.Addition(
                        AstNumber.SimpleNumber.IntegerNumber(1),
                        AstNumber.SimpleNumber.IntegerNumber(2),
                    ),
                ),
            )
        }

        test("chained assignments") {
            val code = "a = b = 3"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("a"),
                    Assignment(
                        Identifier("b"),
                        AstNumber.SimpleNumber.IntegerNumber(3),
                    ),
                ),
            )
        }

        test("assignment with trailing semicolon and whitespace") {
            val code = "  a = 42;  "

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("a"),
                    AstNumber.SimpleNumber.IntegerNumber(42),
                ),
            )
        }
    }

    context("array assignments") {
        test("array index with number") {
            val code = "arr[0] = 42"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        Identifier("arr"),
                        AstNumber.SimpleNumber.IntegerNumber(0),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(42),
                ),
            )
        }

        test("array index with string key") {
            val code = "arr[\"key\"] = \"value\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        Identifier("arr"),
                        StringLiteral("key"),
                    ),
                    StringLiteral("value"),
                ),
            )
        }

        test("array index with identifier") {
            val code = "arr[i] = true"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        Identifier("arr"),
                        Identifier("i"),
                    ),
                    AstBoolean.True,
                ),
            )
        }

        test("array index with expression") {
            val code = "arr[i + 1] = x * 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        Identifier("arr"),
                        ArithmeticExpression.Addition(
                            Identifier("i"),
                            AstNumber.SimpleNumber.IntegerNumber(1),
                        ),
                    ),
                    ArithmeticExpression.Multiplication(
                        Identifier("x"),
                        AstNumber.SimpleNumber.IntegerNumber(2),
                    ),
                ),
            )
        }

        test("multi-dimensional array assignment") {
            val code = "matrix[0][1] = 99"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        ArrayReference(
                            Identifier("matrix"),
                            AstNumber.SimpleNumber.IntegerNumber(0),
                        ),
                        AstNumber.SimpleNumber.IntegerNumber(1),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(99),
                ),
            )
        }

        test("deep multi-dimensional array assignment") {
            val code = "cube[x][y][z] = \"center\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        ArrayReference(
                            ArrayReference(
                                Identifier("cube"),
                                Identifier("x"),
                            ),
                            Identifier("y"),
                        ),
                        Identifier("z"),
                    ),
                    StringLiteral("center"),
                ),
            )
        }
    }

    context("object chain assignments") {
        test("simple property assignment") {
            val code = "user.name = \"Alice\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("user"),
                        Identifier("name"),
                    ),
                    StringLiteral("Alice"),
                ),
            )
        }

        test("nested object property assignment") {
            val code = "user.profile.age = 30"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("user"),
                        ChainedCall(
                            Identifier("profile"),
                            Identifier("age"),
                        ),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(30),
                ),
            )
        }

        test("deep object chain assignment") {
            val code = "a.b.c.d = 1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("a"),
                        ChainedCall(
                            Identifier("b"),
                            ChainedCall(
                                Identifier("c"),
                                Identifier("d"),
                            ),
                        ),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("property assignment to expression value") {
            val code = "point.x = a + b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("point"),
                        Identifier("x"),
                    ),
                    ArithmeticExpression.Addition(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                ),
            )
        }

        test("property assignment with function call value") {
            val code = "config.handler = getHandler()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("config"),
                        Identifier("handler"),
                    ),
                    FunctionCall(
                        Identifier("getHandler"),
                    ),
                ),
            )
        }
    }

    context("function call result assignments") {
        test("property on function call result") {
            val code = "getUser().name = \"Bob\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        FunctionCall(
                            Identifier("getUser"),
                        ),
                        Identifier("name"),
                    ),
                    StringLiteral("Bob"),
                ),
            )
        }

        test("array index on function call result") {
            val code = "getItems()[0] = 100"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        FunctionCall(
                            Identifier("getItems"),
                        ),
                        AstNumber.SimpleNumber.IntegerNumber(0),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(100),
                ),
            )
        }

        test("method call result property assignment") {
            val code = "service.getClient().status = \"active\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("service"),
                        ChainedCall(
                            FunctionCall(
                                Identifier("getClient"),
                            ),
                            Identifier("status"),
                        ),
                    ),
                    StringLiteral("active"),
                ),
            )
        }

        test("function call with arguments property assignment") {
            val code = "findItem(\"id123\").value = 42"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        FunctionCall(
                            Identifier("findItem"),
                            StringLiteral("id123"),
                        ),
                        Identifier("value"),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(42),
                ),
            )
        }

        test("chained function calls property assignment") {
            val code = "getA().getB().value = 5"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        FunctionCall(
                            Identifier("getA"),
                        ),
                        ChainedCall(
                            FunctionCall(
                                Identifier("getB"),
                            ),
                            Identifier("value"),
                        ),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(5),
                ),
            )
        }
    }

    context("mixed and complex assignments") {
        test("array element on object property") {
            val code = "user.roles[0] = \"admin\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("user"),
                        ArrayReference(
                            Identifier("roles"),
                            AstNumber.SimpleNumber.IntegerNumber(0),
                        ),
                    ),
                    StringLiteral("admin"),
                ),
            )
        }

        test("object property on array element") {
            val code = "users[0].name = \"Charlie\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        ArrayReference(
                            Identifier("users"),
                            AstNumber.SimpleNumber.IntegerNumber(0),
                        ),
                        Identifier("name"),
                    ),
                    StringLiteral("Charlie"),
                ),
            )
        }

        test("nested array inside object property on array element") {
            val code = "matrix[i].rows[j] = 0"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        ArrayReference(
                            Identifier("matrix"),
                            Identifier("i"),
                        ),
                        ArrayReference(
                            Identifier("rows"),
                            Identifier("j"),
                        ),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(0),
                ),
            )
        }

        test("array access on function call with object property") {
            val code = "getGroup().members[0].role = \"leader\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        FunctionCall(
                            Identifier("getGroup"),
                        ),
                        ChainedCall(
                            ArrayReference(
                                Identifier("members"),
                                AstNumber.SimpleNumber.IntegerNumber(0),
                            ),
                            Identifier("role"),
                        ),
                    ),
                    StringLiteral("leader"),
                ),
            )
        }

        test("object property on array returned from function") {
            val code = "getUsers()[index].active = true"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        ArrayReference(
                            FunctionCall(
                                Identifier("getUsers"),
                            ),
                            Identifier("index"),
                        ),
                        Identifier("active"),
                    ),
                    AstBoolean.True,
                ),
            )
        }

        test("chained assignment across mixed lvalues") {
            val code = "a.b = c[0] = 10"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("a"),
                        Identifier("b"),
                    ),
                    Assignment(
                        ArrayReference(
                            Identifier("c"),
                            AstNumber.SimpleNumber.IntegerNumber(0),
                        ),
                        AstNumber.SimpleNumber.IntegerNumber(10),
                    ),
                ),
            )
        }

        test("chained assignment with function call result target") {
            val code = "obj.x = arr[1] = getTarget().value = 42"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("obj"),
                        Identifier("x"),
                    ),
                    Assignment(
                        ArrayReference(
                            Identifier("arr"),
                            AstNumber.SimpleNumber.IntegerNumber(1),
                        ),
                        Assignment(
                            ChainedCall(
                                FunctionCall(
                                    Identifier("getTarget"),
                                ),
                                Identifier("value"),
                            ),
                            AstNumber.SimpleNumber.IntegerNumber(42),
                        ),
                    ),
                ),
            )
        }

        test("assign array reference to array reference") {
            val code = "a[0] = b.c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ArrayReference(
                        Identifier("a"),
                        AstNumber.SimpleNumber.IntegerNumber(0),
                    ),
                    ChainedCall(
                        Identifier("b"),
                        Identifier("c"),
                    ),
                ),
            )
        }

        test("assign function call result element to object property") {
            val code = "x.y = getVal()[0]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    ChainedCall(
                        Identifier("x"),
                        Identifier("y"),
                    ),
                    ArrayReference(
                        FunctionCall(
                            Identifier("getVal"),
                        ),
                        AstNumber.SimpleNumber.IntegerNumber(0),
                    ),
                ),
            )
        }
    }

    context("toCode") {
        test("array assignment toCode") {
            val assignment = Assignment(
                ArrayReference(
                    Identifier("arr"),
                    AstNumber.SimpleNumber.IntegerNumber(0),
                ),
                AstNumber.SimpleNumber.IntegerNumber(42),
            )

            assignment.toCode() shouldBe "arr[0] = 42"
        }

        test("object chain assignment toCode") {
            val assignment = Assignment(
                ChainedCall(
                    Identifier("user"),
                    Identifier("name"),
                ),
                StringLiteral("Alice"),
            )

            assignment.toCode() shouldBe "user.name = \"Alice\""
        }

        test("function call property assignment toCode") {
            val assignment = Assignment(
                ChainedCall(
                    FunctionCall(
                        Identifier("getUser"),
                    ),
                    Identifier("name"),
                ),
                StringLiteral("Bob"),
            )

            assignment.toCode() shouldBe "getUser().name = \"Bob\""
        }
    }
})
