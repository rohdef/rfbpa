package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunctionDefinitionTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("Simple function definition") {
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

    test("Function without a name") {
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

    test("Function identifier parameters") {
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

    test("Simple return") {
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

    test("Function object destructure paramter") {
        val code = """
            function total({x, y, z}) {
                return x + y + z;
            }
        """.trimIndent()

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("total") to FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter(TODO("destructure not yet supported")),
                    BlockStatement(
                        Return.Expression(
                            ArithmeticExpression.Addition(
                                Identifier("x"),
                                ArithmeticExpression.Addition(
                                    Identifier("y"),
                                    Identifier("z"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        )
    }

    test("Function array destructure paramter") {
        val code = """
            const fun = function ([a, b, c]) {
                return a * a + b * b - c * c;
            }
        """.trimIndent()

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("fun") to FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter(TODO("destructure not yet supported")),
                    BlockStatement(
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

    test("Function with default value") {
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

    test("Function mix of destructures and identifiers") {
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
                    .withParameter(TODO("Destructuring not yet supported")),
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


    test("Function mixed parameters with default values") {
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
                            Identifier("j"),
                            ArithmeticExpression.Addition(
                                Identifier("f"),
                                Identifier("j"),
                            ),
                        )
                    ),
                ),
            ),
        )
    }

    test("Function with body") {
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

    test("Function with one line body") {
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

    test("Function with longer body") {
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
                        TODO("If not yet supported"),
                        TODO("If not yet supported"),
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