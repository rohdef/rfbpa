package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.logic.IfStatement
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Function definition")
class FunctionDefinitionTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("simple") {
        val code = "function dummy() {}"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("dummy") to FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(),
                ),
            )
        )
    }

    test("without a name") {
        val code = "const life = function () { }"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Constant(
                Identifier("life") to FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(),
                ),
            )
        )
    }

    test("simple return") {
        val code = """
            function doNothing() {
                return   ;
            }
        """.trimIndent()

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("doNothing") to FunctionExpression(
                    FunctionExpression.Parameters(),
                    BlockStatement(
                        Return.Void,
                    ),
                ),
            )
        )
    }

    context("parameters") {
        test("identifiers") {
            val code = """
                function this_n4m3_is_cra${"$"}y(x, y, z) {
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Variable(
                    Identifier("this_n4m3_is_cra\$y") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter(Identifier("x"))
                            .withParameter(Identifier("y"))
                            .withParameter(Identifier("z")),
                        BlockStatement(),
                    ),
                )
            )
        }

        test("with default value") {
            val code = """
                function dummy(j = "BeeJay") {
                    return j + j + j;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Variable(
                    Identifier("dummy") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter(Identifier("j"), StringLiteral("BeeJay")),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    ArithmeticExpression.Addition(
                                        Identifier("j"),
                                        Identifier("j"),
                                    ),
                                    Identifier("j"),
                                ),
                            ),
                        ),
                    ),
                )
            )
        }

        test("mixed and with default values") {
            val code = """
                function (j = "BeeJay", f) {
                    return j + f + j;
                }
            """.trimIndent()

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
                                ArithmeticExpression.Addition(
                                    Identifier("j"),
                                    Identifier("f"),
                                ),
                                Identifier("j"),
                            )
                        ),
                    ),
                ),
            )
        }

        context("destructuring") {
            context("objects") {
                test("empty") {
                    val code = """
                        function sayHi({ }) {
                            return "Hello " + "world!";
                        }
                    """.trimIndent()

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        Declaration.Variable(
                            Identifier("sayHi") to FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter(
                                        FunctionExpression.Parameter.ObjectDestructure(),
                                    ),
                                BlockStatement(
                                    Return.Expression(
                                        ArithmeticExpression.Addition(
                                            StringLiteral("Hello "),
                                            StringLiteral("world!"),
                                        ),
                                    ),
                                ),
                            ),
                        )
                    )
                }

                test("single parameter") {
                    val code = """
                        function sayHi({x}) {
                            return "Hello " + x;
                        }
                    """.trimIndent()

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        Declaration.Variable(
                            Identifier("sayHi") to FunctionExpression(
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
                    )
                }

                test("multiple parameters") {
                    val code = """
                        function total({x, y , z }) {
                            return x + y + z;
                        }
                    """.trimIndent()

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        Declaration.Variable(
                            Identifier("total") to FunctionExpression(
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
                    )
                }
            }

            context("arrays") {
                test("empty") {
                    val code = """
                        const fun = function ([]) {}
                    """.trimIndent()

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        Declaration.Constant(
                            Identifier("fun") to FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter(
                                        FunctionExpression.Parameter.ArrayDestructure(),
                                    ),
                                BlockStatement(),
                            ),
                        ),
                    )
                }

                test("single element") {
                    val code = """
                        const fun = function ([a]) {
                            return a * a;
                        }
                    """.trimIndent()

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        Declaration.Constant(
                            Identifier("fun") to FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter(
                                        FunctionExpression.Parameter.ArrayDestructure(
                                            FunctionExpression.Parameter.IdentifierParameter("a"),
                                        )
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
                        ),
                    )
                }

                test("multiple elements") {
                    val code = """
                        const fun = function ([a, b , c  ]) {
                            return a * a + b * b - c * c;
                        }
                    """.trimIndent()

                    val result = parser.run(code)
                    val program = result.stackTop

                    program shouldBe Program(
                        Declaration.Constant(
                            Identifier("fun") to FunctionExpression(
                                FunctionExpression.Parameters()
                                    .withParameter(
                                        FunctionExpression.Parameter.ArrayDestructure(
                                            FunctionExpression.Parameter.IdentifierParameter("a"),
                                            FunctionExpression.Parameter.IdentifierParameter("b"),
                                            FunctionExpression.Parameter.IdentifierParameter("c"),
                                        )
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
                        ),
                    )
                }
            }

            test("mix of array and object") {
                val code = """
                    function ({a, b, c}, [x, y, z]) {
                        return j + j + j;
                    }
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter(
                                FunctionExpression.Parameter.ObjectDestructure(
                                    FunctionExpression.Parameter.IdentifierParameter("a"),
                                    FunctionExpression.Parameter.IdentifierParameter("b"),
                                    FunctionExpression.Parameter.IdentifierParameter("c"),
                                )
                            )
                            .withParameter(
                                FunctionExpression.Parameter.ArrayDestructure(
                                    FunctionExpression.Parameter.IdentifierParameter("x"),
                                    FunctionExpression.Parameter.IdentifierParameter("y"),
                                    FunctionExpression.Parameter.IdentifierParameter("z"),
                                )
                            ),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    ArithmeticExpression.Addition(
                                        Identifier("j"),
                                        Identifier("j"),
                                    ),
                                    Identifier("j"),
                                ),
                            ),
                        ),
                    )
                )
            }

            test("with default values") {
                val code = """
                    function ({a, b=32, c}, [x=true, y, z="hey"]) {}
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter(
                                FunctionExpression.Parameter.ObjectDestructure(
                                    FunctionExpression.Parameter.IdentifierParameter("a"),
                                    FunctionExpression.Parameter.IdentifierParameter(
                                        "b", Number.SimpleNumber.IntegerNumber(32),
                                    ),
                                    FunctionExpression.Parameter.IdentifierParameter("c"),
                                ),
                            )
                            .withParameter(
                                FunctionExpression.Parameter.ArrayDestructure(
                                    FunctionExpression.Parameter.IdentifierParameter(
                                        Identifier("x"), Boolean.True,
                                    ),
                                    FunctionExpression.Parameter.IdentifierParameter(Identifier("y")),
                                    FunctionExpression.Parameter.IdentifierParameter(
                                        Identifier("z"), StringLiteral("hey"),
                                    ),
                                ),
                            ),
                        BlockStatement(),
                    )
                )
            }

            test("nested array and object") {
                val code = """
                    function bah({a, b=32, c=[x=true, y, z="hey"]}, [w={n, m}, e, r=[l, k]]) {}
                """.trimIndent()

                val result = parser.run(code)
                val program = result.stackTop

                program shouldBe Program(
                    Declaration.Variable(
                        Identifier("bah") to FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(
                                    FunctionExpression.Parameter.ObjectDestructure(
                                        FunctionExpression.Parameter.IdentifierParameter("a"),
                                        FunctionExpression.Parameter.IdentifierParameter(
                                            "b",
                                            Number.SimpleNumber.IntegerNumber(32)
                                        ),
                                        FunctionExpression.Parameter.IdentifierParameter(
                                            "c",
                                            FunctionExpression.Parameter.ArrayDestructure(
                                                FunctionExpression.Parameter.IdentifierParameter("x", Boolean.True),
                                                FunctionExpression.Parameter.IdentifierParameter("y"),
                                                FunctionExpression.Parameter.IdentifierParameter(
                                                    "z",
                                                    StringLiteral("hey")
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
                                            "r", FunctionExpression.Parameter.ArrayDestructure(
                                                FunctionExpression.Parameter.IdentifierParameter("l"),
                                                FunctionExpression.Parameter.IdentifierParameter("k"),
                                            )
                                        ),
                                    ),
                                ),
                            BlockStatement(),
                        ),
                    ),
                )
            }
        }
    }

    test("with body") {
        val code = "function identity(x) { return x; }"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("identity") to FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter(Identifier("x")),
                    BlockStatement(
                        Return.Expression(
                            Identifier("x"),
                        ),
                    ),
                ),
            )
        )
    }

    test("with one line body") {
        val code = "function fromIso(s){var d=s.split('-');return newDate(d[0],d[1],d[2]);}"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("fromIso") to FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter(Identifier("s")),
                    BlockStatement(
                        Declaration.Variable(
                            Identifier("d") to ChainedCall(
                                Identifier("s"),
                                FunctionCall(
                                    Identifier("split"),
                                    StringLiteral("-")
                                ),
                            ),
                        ),
                        Return.Expression(
                            FunctionCall(
                                Identifier("newDate"),
                                ArrayReference(
                                    Identifier("d"),
                                    Number.SimpleNumber.IntegerNumber(0),
                                ),
                                ArrayReference(
                                    Identifier("d"),
                                    Number.SimpleNumber.IntegerNumber(1),
                                ),
                                ArrayReference(
                                    Identifier("d"),
                                    Number.SimpleNumber.IntegerNumber(2),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        )
    }

    test("with longer body") {
        val code = """
            function tTal(x){
              var n=Math.round((Number(x)||0)*100)/100;
              var s=n.toFixed(2);
              if(s.charAt(s.length-1)==='0') s=s.slice(0,-1);               /* 12,50 → 12,5 */
              if(s.charAt(s.length-1)==='0'&&s.charAt(s.length-2)==='.') s=s.slice(0,-2);  /* 8,0 → 8 */
              return s.replace('.',',');
            }
        """.trimIndent()

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("tTal") to FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter(Identifier("x")),
                    BlockStatement(
                        Declaration.Variable(
                            Identifier("n") to ArithmeticExpression.Division(
                                ChainedCall(
                                    Identifier("Math"),
                                    FunctionCall(
                                        Identifier("round"),
                                        ArithmeticExpression.Multiplication(
                                            EnclosedExpression(
                                                LogicExpression.LogicalOr(
                                                    FunctionCall(
                                                        Identifier("Number"),
                                                        Identifier("x"),
                                                    ),
                                                    Number.SimpleNumber.IntegerNumber(0),
                                                )
                                            ),
                                            Number.SimpleNumber.IntegerNumber(100),
                                        )
                                    ),
                                ),
                                Number.SimpleNumber.IntegerNumber(100),
                            ),
                        ),
                        Declaration.Variable(
                            Identifier("s") to ChainedCall(
                                Identifier("n"),
                                FunctionCall(
                                    Identifier("toFixed"),
                                    Number.SimpleNumber.IntegerNumber(2),
                                ),
                            ),
                        ),
                        IfStatement(
                            LogicExpression.NonCoercingEquals(
                                ChainedCall(
                                    Identifier("s"),
                                    FunctionCall(
                                        Identifier("charAt"),
                                        ArithmeticExpression.Subtraction(
                                            ChainedCall(
                                                Identifier("s"),
                                                Identifier("length"),
                                            ),
                                            Number.SimpleNumber.IntegerNumber(1)
                                        ),
                                    ),
                                ),
                                StringLiteral("0"),
                            ),
                            Assignment(
                                Identifier("s"),
                                ChainedCall(
                                    Identifier("s"),
                                    FunctionCall(
                                        Identifier("slice"),
                                        Number.SimpleNumber.IntegerNumber(0),
                                        PrefixExpression.Minus(
                                            Number.SimpleNumber.IntegerNumber(1),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                        IfStatement(
                            LogicExpression.LogicalAnd(
                                LogicExpression.NonCoercingEquals(
                                    ChainedCall(
                                        Identifier("s"),
                                        FunctionCall(
                                            Identifier("charAt"),
                                            ArithmeticExpression.Subtraction(
                                                ChainedCall(
                                                    Identifier("s"),
                                                    Identifier("length"),
                                                ),
                                                Number.SimpleNumber.IntegerNumber(1)
                                            ),
                                        ),
                                    ),
                                    StringLiteral("0"),
                                ),
                                LogicExpression.NonCoercingEquals(
                                    ChainedCall(
                                        Identifier("s"),
                                        FunctionCall(
                                            Identifier("charAt"),
                                            ArithmeticExpression.Subtraction(
                                                ChainedCall(
                                                    Identifier("s"),
                                                    Identifier("length"),
                                                ),
                                                Number.SimpleNumber.IntegerNumber(2)
                                            ),
                                        ),
                                    ),
                                    StringLiteral("."),
                                ),
                            ),
                            Assignment(
                                Identifier("s"),
                                ChainedCall(
                                    Identifier("s"),
                                    FunctionCall(
                                        Identifier("slice"),
                                        Number.SimpleNumber.IntegerNumber(0),
                                        PrefixExpression.Minus(
                                            Number.SimpleNumber.IntegerNumber(2),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                        Return.Expression(
                            ChainedCall(
                                Identifier("s"),
                                FunctionCall(
                                    Identifier("replace"),
                                    StringLiteral("."),
                                    StringLiteral(","),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
})