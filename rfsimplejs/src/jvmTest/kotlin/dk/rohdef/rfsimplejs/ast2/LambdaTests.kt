package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.Declaration
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Undefined
import dk.rohdef.rfsimplejs.ast2.statement.logic.IfStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.TernaryExpression
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Lambdas")
class LambdaTests : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("basic syntax") {
        test("no parameters - empty block body") {
            val code = "() => {}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(),
                ),
            )
        }

        test("no parameters - block body with return void") {
            val code = "() => { return; }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Void,
                    ),
                ),
            )
        }

        test("no parameters - expression body") {
            val code = "() => 42"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Expression(
                            Number.SimpleNumber.IntegerNumber(42),
                        ),
                    ),
                ),
            )
        }

        test("single parameter without parentheses - expression body") {
            val code = "x => x"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            Identifier("x"),
                        ),
                    ),
                ),
            )
        }

        test("single parameter without parentheses - block body") {
            val code = "x => { return x; }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            Identifier("x"),
                        ),
                    ),
                ),
            )
        }

        test("single parameter with parentheses - expression body") {
            val code = "(x) => x * 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Multiplication(
                                Identifier("x"),
                                Number.SimpleNumber.IntegerNumber(2),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("single parameter with parentheses - block body") {
            val code = "(x) => { return x + 1; }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                Identifier("x"),
                                Number.SimpleNumber.IntegerNumber(1),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("multiple parameters - expression body") {
            val code = "(x, y) => x + y"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x")
                        .withParameter("y"),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                Identifier("x"),
                                Identifier("y"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("multiple parameters - block body") {
            val code = "(x, y, z) => { return x + y + z; }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x")
                        .withParameter("y")
                        .withParameter("z"),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                ArithmeticExpression.Addition(
                                    Identifier("x"),
                                    Identifier("y"),
                                ),
                                Identifier("z"),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("parameters") {
        test("identifiers with special characters") {
            val code = "(a, _b, \$c) => a + _b + \$c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("a")
                        .withParameter("_b")
                        .withParameter("\$c"),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                ArithmeticExpression.Addition(
                                    Identifier("a"),
                                    Identifier("_b"),
                                ),
                                Identifier("\$c"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("with default value") {
            val code = "(j = \"BeeJay\") => j + j"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter(Identifier("j"), StringLiteral("BeeJay")),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                Identifier("j"),
                                Identifier("j"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("mixed and with default values") {
            val code = "(j = \"BeeJay\", f) => j + f"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter(Identifier("j"), StringLiteral("BeeJay"))
                        .withParameter(Identifier("f")),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                Identifier("j"),
                                Identifier("f"),
                            ),
                        ),
                    ),
                ),
            )
        }

        context("destructuring") {
            context("objects") {
                test("empty") {
                    val code = "({}) => \"Hello world!\""

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ObjectDestructure(),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    StringLiteral("Hello world!"),
                                ),
                            ),
                        ),
                    )
                }

                test("single parameter") {
                    val code = "({x}) => \"Hello \" + x"

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ObjectDestructure(
                                        FunctionExpression.Parameter.IdentifierParameter("x"),
                                    ),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        StringLiteral("Hello "),
                                        Identifier("x"),
                                    ),
                                ),
                            ),
                        ),
                    )
                }

                test("multiple parameters") {
                    val code = "({x, y, z}) => x + y + z"

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ObjectDestructure(
                                        FunctionExpression.Parameter.IdentifierParameter("x"),
                                        FunctionExpression.Parameter.IdentifierParameter("y"),
                                        FunctionExpression.Parameter.IdentifierParameter("z"),
                                    ),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        ArithmeticExpression.Addition(
                                            Identifier("x"),
                                            Identifier("y"),
                                        ),
                                        Identifier("z"),
                                    ),
                                ),
                            ),
                        ),
                    )
                }

                test("with default values") {
                    val code = "({x = 10, y = 20}) => x + y"

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ObjectDestructure(
                                        FunctionExpression.Parameter.IdentifierParameter(
                                            Identifier("x"),
                                            Number.SimpleNumber.IntegerNumber(10),
                                        ),
                                        FunctionExpression.Parameter.IdentifierParameter(
                                            Identifier("y"),
                                            Number.SimpleNumber.IntegerNumber(20),
                                        ),
                                    ),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        Identifier("x"),
                                        Identifier("y"),
                                    ),
                                ),
                            ),
                        ),
                    )
                }
            }

            context("arrays") {
                test("empty") {
                    val code = "([]) => 42"

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ArrayDestructure(),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    Number.SimpleNumber.IntegerNumber(42),
                                ),
                            ),
                        ),
                    )
                }

                test("single element") {
                    val code = "([a]) => a * a"

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ArrayDestructure(
                                        FunctionExpression.Parameter.IdentifierParameter("a"),
                                    ),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Multiplication(
                                        Identifier("a"),
                                        Identifier("a"),
                                    ),
                                ),
                            ),
                        ),
                    )
                }

                test("multiple elements") {
                    val code = "([a, b, c]) => a * a + b * b - c * c"

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ArrayDestructure(
                                        FunctionExpression.Parameter.IdentifierParameter("a"),
                                        FunctionExpression.Parameter.IdentifierParameter("b"),
                                        FunctionExpression.Parameter.IdentifierParameter("c"),
                                    ),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Subtraction(
                                        ArithmeticExpression.Addition(
                                            ArithmeticExpression.Multiplication(
                                                Identifier("a"),
                                                Identifier("a"),
                                            ),
                                            ArithmeticExpression.Multiplication(
                                                Identifier("b"),
                                                Identifier("b"),
                                            ),
                                        ),
                                        ArithmeticExpression.Multiplication(
                                            Identifier("c"),
                                            Identifier("c"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
                }

                test("with default values") {
                    val code = "([a = 10, b = 20]) => a + b"

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ArrayDestructure(
                                        FunctionExpression.Parameter.IdentifierParameter(
                                            Identifier("a"),
                                            Number.SimpleNumber.IntegerNumber(10),
                                        ),
                                        FunctionExpression.Parameter.IdentifierParameter(
                                            Identifier("b"),
                                            Number.SimpleNumber.IntegerNumber(20),
                                        ),
                                    ),
                                ),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        Identifier("a"),
                                        Identifier("b"),
                                    ),
                                ),
                            ),
                        ),
                    )
                }
            }

            test("nested array and object") {
                val code = "({a, b=32, c=[x=true, y, z=\"hey\"]}, [w={n, m}, e, r=[l, k]]) => a"

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter(
                                FunctionExpression.Parameter.ObjectDestructure(
                                    FunctionExpression.Parameter.IdentifierParameter("a"),
                                    FunctionExpression.Parameter.IdentifierParameter(
                                        "b",
                                        Number.SimpleNumber.IntegerNumber(32),
                                    ),
                                    FunctionExpression.Parameter.IdentifierParameter(
                                        "c",
                                        FunctionExpression.Parameter.ArrayDestructure(
                                            FunctionExpression.Parameter.IdentifierParameter("x", Boolean.True),
                                            FunctionExpression.Parameter.IdentifierParameter("y"),
                                            FunctionExpression.Parameter.IdentifierParameter(
                                                "z",
                                                StringLiteral("hey"),
                                            ),
                                        ),
                                    ),
                                ),
                            )
                            .withParameter(
                                FunctionExpression.Parameter.ArrayDestructure(
                                    FunctionExpression.Parameter.IdentifierParameter(
                                        "w",
                                        FunctionExpression.Parameter.ObjectDestructure(
                                            FunctionExpression.Parameter.IdentifierParameter("n"),
                                            FunctionExpression.Parameter.IdentifierParameter("m"),
                                        ),
                                    ),
                                    FunctionExpression.Parameter.IdentifierParameter("e"),
                                    FunctionExpression.Parameter.IdentifierParameter(
                                        "r",
                                        FunctionExpression.Parameter.ArrayDestructure(
                                            FunctionExpression.Parameter.IdentifierParameter("l"),
                                            FunctionExpression.Parameter.IdentifierParameter("k"),
                                        ),
                                    ),
                                ),
                            ),
                        BlockStatement(
                            Return.Expression(
                                Identifier("a"),
                            ),
                        ),
                    ),
                )
            }
        }
    }

    context("expression body types") {
        test("boolean, null, and undefined literals") {
            val trueResult = parser.run("() => true").stackTop
            trueResult shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Expression(Boolean.True),
                    ),
                ),
            )

            val falseResult = parser.run("() => false").stackTop
            falseResult shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Expression(Boolean.False),
                    ),
                ),
            )

            val nullResult = parser.run("() => null").stackTop
            nullResult shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Expression(Null),
                    ),
                ),
            )

            val undefinedResult = parser.run("() => undefined").stackTop
            undefinedResult shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Expression(Undefined),
                    ),
                ),
            )
        }

        test("binary logic expression") {
            val code = "(a, b) => a === b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("a")
                        .withParameter("b"),
                    BlockStatement(
                        Return.Expression(
                            LogicExpression.NonCoercingEquals(
                                Identifier("a"),
                                Identifier("b"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("ternary expression") {
            val code = "x => x ? 1 : 0"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            TernaryExpression(
                                Identifier("x"),
                                Number.SimpleNumber.IntegerNumber(1),
                                Number.SimpleNumber.IntegerNumber(0),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("array literal") {
            val code = "x => [x, x * 2]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            ArrayLiteral(
                                Identifier("x"),
                                ArithmeticExpression.Multiplication(
                                    Identifier("x"),
                                    Number.SimpleNumber.IntegerNumber(2),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("enclosed object literal") {
            val code = "() => ({ day: \"Tag\", bar: 19 })"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Expression(
                            EnclosedExpression(
                                ObjectLiteral(
                                    Identifier("day") to StringLiteral("Tag"),
                                    Identifier("bar") to Number.SimpleNumber.IntegerNumber(19),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("chained call") {
            val code = "s => s.trim()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("s"),
                    BlockStatement(
                        Return.Expression(
                            ChainedCall(
                                Identifier("s"),
                                FunctionCall(
                                    Identifier("trim"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("enclosed expression") {
            val code = "x => (x + 1)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            EnclosedExpression(
                                ArithmeticExpression.Addition(
                                    Identifier("x"),
                                    Number.SimpleNumber.IntegerNumber(1),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("block bodies with multiple statements") {
        test("variable declaration and return") {
            val code = """
                (x, y) => {
                    var sum = x + y;
                    return sum;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x")
                        .withParameter("y"),
                    BlockStatement(
                        Declaration.Variable(
                            Identifier("sum") to ArithmeticExpression.Addition(
                                Identifier("x"),
                                Identifier("y"),
                            ),
                        ),
                        Return.Expression(
                            Identifier("sum"),
                        ),
                    ),
                ),
            )
        }

        test("conditional logic in body") {
            val code = """
                x => {
                    if (x === 0) {
                        return true;
                    }
                    return false;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        IfStatement(
                            LogicExpression.NonCoercingEquals(
                                Identifier("x"),
                                Number.SimpleNumber.IntegerNumber(0),
                            ),
                            BlockStatement(
                                Return.Expression(
                                    Boolean.True,
                                ),
                            ),
                        ),
                        Return.Expression(
                            Boolean.False,
                        ),
                    ),
                ),
            )
        }
    }

    context("declarations and assignments") {
        test("const declaration") {
            val code = "const double = x => x * 2;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Constant(
                    Identifier("double") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("x"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Multiplication(
                                    Identifier("x"),
                                    Number.SimpleNumber.IntegerNumber(2),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("let declaration") {
            val code = "let add = (a, b) => a + b;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Let(
                    Identifier("add") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("a")
                            .withParameter("b"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    Identifier("a"),
                                    Identifier("b"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("var declaration") {
            val code = "var identity = (x) => { return x; };"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Variable(
                    Identifier("identity") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("x"),
                        BlockStatement(
                            Return.Expression(
                                Identifier("x"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("assignment") {
            val code = "fn = (x) => x + 1;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("fn"),
                    FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("x"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    Identifier("x"),
                                    Number.SimpleNumber.IntegerNumber(1),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("one shot") {
        test("by traditional function") {
            val text = "(function() {})()"

            val result = parser.run(text)
                .stackTop

            result shouldBe Program(
                FunctionCall(
                    EnclosedExpression(
                        FunctionExpression(
                            FunctionExpression.Parameters(),
                            BlockStatement(),
                        ),
                    ),
                ),
            )
        }

        test("by actual lambda") {
            val code = "(() => 42)()"

            val result = parser.run(code)
                .stackTop

            result shouldBe Program(
                FunctionCall(
                    EnclosedExpression(
                        FunctionExpression(
                            FunctionExpression.Parameters(),
                            BlockStatement(
                                Return.Expression(
                                    Number.SimpleNumber.IntegerNumber(42),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("by actual lambda with arguments") {
            val code = "((x, y) => x + y)(2, 3)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionCall(
                    EnclosedExpression(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter("x")
                                .withParameter("y"),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        Identifier("x"),
                                        Identifier("y"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    Number.SimpleNumber.IntegerNumber(2),
                    Number.SimpleNumber.IntegerNumber(3),
                ),
            )
        }

        test("by actual lambda with block body") {
            val code = "((x) => { return x * 2; })(21)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionCall(
                    EnclosedExpression(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter("x"),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Multiplication(
                                        Identifier("x"),
                                        Number.SimpleNumber.IntegerNumber(2),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    Number.SimpleNumber.IntegerNumber(21),
                ),
            )
        }

        test("without parameter parentheses in one shot") {
            val code = "(x => x + 1)(5)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionCall(
                    EnclosedExpression(
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter("x"),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        Identifier("x"),
                                        Number.SimpleNumber.IntegerNumber(1),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    Number.SimpleNumber.IntegerNumber(5),
                ),
            )
        }
    }

    context("higher order functions and callbacks") {
        test("lambda as function argument") {
            val code = "map(x => x * 2)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionCall(
                    Identifier("map"),
                    FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("x"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Multiplication(
                                    Identifier("x"),
                                    Number.SimpleNumber.IntegerNumber(2),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("lambda in chained call") {
            val code = "items.filter(x => x > 0)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ChainedCall(
                    Identifier("items"),
                    FunctionCall(
                        Identifier("filter"),
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter("x"),
                            BlockStatement(
                                Return.Expression(
                                    LogicExpression.GreaterThan(
                                        Identifier("x"),
                                        Number.SimpleNumber.IntegerNumber(0),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("lambda with multiple arguments in chained call") {
            val code = "arr.reduce((acc, curr) => acc + curr, 0)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ChainedCall(
                    Identifier("arr"),
                    FunctionCall(
                        Identifier("reduce"),
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter("acc")
                                .withParameter("curr"),
                            BlockStatement(
                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        Identifier("acc"),
                                        Identifier("curr"),
                                    ),
                                ),
                            ),
                        ),
                        Number.SimpleNumber.IntegerNumber(0),
                    ),
                ),
            )
        }
    }

    context("currying and nested lambdas") {
        test("curried two-level lambda") {
            val code = "x => y => x + y"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter("y"),
                                BlockStatement(
                                    Return.Expression(
                                        ArithmeticExpression.Addition(
                                            Identifier("x"),
                                            Identifier("y"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("curried three-level lambda") {
            val code = "a => b => c => a + b + c"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("a"),
                    BlockStatement(
                        Return.Expression(
                            FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter("b"),
                                BlockStatement(
                                    Return.Expression(
                                        FunctionExpression(
                                            FunctionExpression.Parameters()
                                                .withParameter("c"),
                                            BlockStatement(
                                                Return.Expression(
                                                    ArithmeticExpression.Addition(
                                                        ArithmeticExpression.Addition(
                                                            Identifier("a"),
                                                            Identifier("b"),
                                                        ),
                                                        Identifier("c"),
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("curried with block body") {
            val code = "(x) => (y) => { return x + y; }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter("y"),
                                BlockStatement(
                                    Return.Expression(
                                        ArithmeticExpression.Addition(
                                            Identifier("x"),
                                            Identifier("y"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("lambda returning lambda from block body") {
            val code = """
                x => {
                    return y => x + y;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter("y"),
                                BlockStatement(
                                    Return.Expression(
                                        ArithmeticExpression.Addition(
                                            Identifier("x"),
                                            Identifier("y"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("in data structures") {
        test("in array literal") {
            val code = "[x => x + 1, (a, b) => a + b]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArrayLiteral(
                    FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("x"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    Identifier("x"),
                                    Number.SimpleNumber.IntegerNumber(1),
                                ),
                            ),
                        ),
                    ),
                    FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("a")
                            .withParameter("b"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    Identifier("a"),
                                    Identifier("b"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("in object literal") {
            val code = "{ double: x => x * 2, add: (a, b) => a + b }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ObjectLiteral(
                    Identifier("double") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("x"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Multiplication(
                                    Identifier("x"),
                                    Number.SimpleNumber.IntegerNumber(2),
                                ),
                            ),
                        ),
                    ),
                    Identifier("add") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter("a")
                            .withParameter("b"),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    Identifier("a"),
                                    Identifier("b"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("whitespace and formatting") {
        test("multiline parameter list and body") {
            val code = """
                (
                    a,
                    b
                ) => {
                    return a + b;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("a")
                        .withParameter("b"),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                Identifier("a"),
                                Identifier("b"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("spaces around arrow") {
            val code = "x   =>   x"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("x"),
                    BlockStatement(
                        Return.Expression(
                            Identifier("x"),
                        ),
                    ),
                ),
            )
        }
    }
})