package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Undefined
import dk.rohdef.rfsimplejs.ast2.statement.logic.ForInStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.IfStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.TernaryExpression
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("For-in loop")
class ForInTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("toCode") {
        test("ForInStatement toCode with block") {
            val statement = ForInStatement(
                Declaration.Variable(
                    Identifier("k") to Undefined,
                ),
                Identifier("v"),
                BlockStatement(
                    ChainedCall(
                        Identifier("console"),
                        FunctionCall(
                            Identifier("log"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )

            statement.toCode() shouldBe """
                for (var k = undefined; in v) {
                    console.log(k)
                }
            """.trimIndent()
        }
    }

    context("var declaration in header") {
        test("compact with single statement body") {
            val code = "for(var k in v) console.log(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    ChainedCall(
                        Identifier("console"),
                        FunctionCall(
                            Identifier("log"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )
        }

        test("spaced with single statement body") {
            val code = "for (var k in v) console.log(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    ChainedCall(
                        Identifier("console"),
                        FunctionCall(
                            Identifier("log"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )
        }

        test("block body with single statement") {
            val code = """
                for (var k in v) {
                    console.log(k);
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    BlockStatement(
                        ChainedCall(
                            Identifier("console"),
                            FunctionCall(
                                Identifier("log"),
                                Identifier("k"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("block body with multiple statements") {
            val code = """
                for (var key in object) {
                    console.log(key);
                    count = count + 1;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("key") to Undefined,
                    ),
                    Identifier("object"),
                    BlockStatement(
                        ChainedCall(
                            Identifier("console"),
                            FunctionCall(
                                Identifier("log"),
                                Identifier("key"),
                            ),
                        ),
                        Assignment(
                            Identifier("count"),
                            dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression.Addition(
                                Identifier("count"),
                                Number.SimpleNumber.IntegerNumber(1),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("empty block body") {
            val code = "for (var k in v) {}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    BlockStatement(),
                ),
            )
        }
    }

    context("let declaration in header") {
        test("single statement body") {
            val code = "for (let k in v) console.log(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Let(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    ChainedCall(
                        Identifier("console"),
                        FunctionCall(
                            Identifier("log"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )
        }

        test("block body") {
            val code = """
                for (let prop in target) {
                    process(prop);
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Let(
                        Identifier("prop") to Undefined,
                    ),
                    Identifier("target"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("process"),
                            Identifier("prop"),
                        ),
                    ),
                ),
            )
        }
    }

    context("const declaration in header") {
        test("single statement body") {
            val code = "for (const k in v) console.log(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Constant(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    ChainedCall(
                        Identifier("console"),
                        FunctionCall(
                            Identifier("log"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )
        }

        test("block body") {
            val code = """
                for (const key in obj) {
                    foo(key);
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Constant(
                        Identifier("key") to Undefined,
                    ),
                    Identifier("obj"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("foo"),
                            Identifier("key"),
                        ),
                    ),
                ),
            )
        }
    }

    context("unqualified identifier in header") {
        test("single statement body") {
            val code = "for (k in v) console.log(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Identifier("k"),
                    Identifier("v"),
                    ChainedCall(
                        Identifier("console"),
                        FunctionCall(
                            Identifier("log"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )
        }

        test("block body") {
            val code = """
                for (k in v) {
                    handle(k);
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Identifier("k"),
                    Identifier("v"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("handle"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )
        }
    }

    context("various target expressions") {
        test("object literal target") {
            val code = "for (var k in { a: 1, b: 2 }) foo(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    ObjectLiteral(
                        Identifier("a") to Number.SimpleNumber.IntegerNumber(1),
                        Identifier("b") to Number.SimpleNumber.IntegerNumber(2),
                    ),
                    FunctionCall(
                        Identifier("foo"),
                        Identifier("k"),
                    ),
                ),
            )
        }

        test("array literal target") {
            val code = "for (var i in [1, 2, 3]) foo(i);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("i") to Undefined,
                    ),
                    ArrayLiteral(
                        Number.SimpleNumber.IntegerNumber(1),
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                    FunctionCall(
                        Identifier("foo"),
                        Identifier("i"),
                    ),
                ),
            )
        }

        test("function call target") {
            val code = "for (var k in getItems()) process(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    FunctionCall(
                        Identifier("getItems"),
                    ),
                    FunctionCall(
                        Identifier("process"),
                        Identifier("k"),
                    ),
                ),
            )
        }

        test("chained call target") {
            val code = "for (var k in Object.keys(v)) foo(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    ChainedCall(
                        Identifier("Object"),
                        FunctionCall(
                            Identifier("keys"),
                            Identifier("v"),
                        ),
                    ),
                    FunctionCall(
                        Identifier("foo"),
                        Identifier("k"),
                    ),
                ),
            )
        }

        test("enclosed ternary expression target") {
            val code = "for (var k in (condition ? a : b)) foo(k);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    EnclosedExpression(
                        TernaryExpression(
                            Identifier("condition"),
                            Identifier("a"),
                            Identifier("b"),
                        ),
                    ),
                    FunctionCall(
                        Identifier("foo"),
                        Identifier("k"),
                    ),
                ),
            )
        }
    }

    context("control flow within for-in loop") {
        test("break in for-in loop") {
            val code = """
                for (var k in v) {
                    if (k === "stop") break;
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    BlockStatement(
                        IfStatement(
                            LogicExpression.NonCoercingEquals(
                                Identifier("k"),
                                StringLiteral("stop"),
                            ),
                            Break,
                        ),
                    ),
                ),
            )
        }

        test("continue in for-in loop") {
            val code = """
                for (var k in v) {
                    if (k === "skip") continue;
                    console.log(k);
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    BlockStatement(
                        IfStatement(
                            LogicExpression.NonCoercingEquals(
                                Identifier("k"),
                                StringLiteral("skip"),
                            ),
                            Continue,
                        ),
                        ChainedCall(
                            Identifier("console"),
                            FunctionCall(
                                Identifier("log"),
                                Identifier("k"),
                            ),
                        ),
                    ),
                ),
            )
        }

        test("return in for-in loop within function") {
            val code = """
                function findKey(obj) {
                    for (var k in obj) {
                        return k;
                    }
                }
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Variable(
                    Identifier("findKey") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter(Identifier("obj")),
                        BlockStatement(
                            ForInStatement(
                                Declaration.Variable(
                                    Identifier("k") to Undefined,
                                ),
                                Identifier("obj"),
                                BlockStatement(
                                    Return.Expression(
                                        Identifier("k"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    context("nested and combined structures") {
        test("nested for-in loops") {
            val code = "for (var k1 in v1) for (var k2 in v2) foo(k1, k2);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k1") to Undefined,
                    ),
                    Identifier("v1"),
                    ForInStatement(
                        Declaration.Variable(
                            Identifier("k2") to Undefined,
                        ),
                        Identifier("v2"),
                        FunctionCall(
                            Identifier("foo"),
                            Identifier("k1"),
                            Identifier("k2"),
                        ),
                    ),
                ),
            )
        }

        test("for-in loop with if statement body") {
            val code = """
                for (var k in v) if (k === "length") output = k;
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    IfStatement(
                        LogicExpression.NonCoercingEquals(
                            Identifier("k"),
                            StringLiteral("length"),
                        ),
                        Assignment(
                            Identifier("output"),
                            Identifier("k"),
                        ),
                    ),
                ),
            )
        }

        test("for-in loop with block followed by function call without separator") {
            val code = "for (var k in v) { foo(k); } bar();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("foo"),
                            Identifier("k"),
                        ),
                    ),
                ),
                FunctionCall(
                    Identifier("bar"),
                ),
            )
        }

        test("for-in loop with block followed by variable declaration") {
            val code = "for (var k in v) { foo(k); } var x = 1;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForInStatement(
                    Declaration.Variable(
                        Identifier("k") to Undefined,
                    ),
                    Identifier("v"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("foo"),
                            Identifier("k"),
                        ),
                    ),
                ),
                Declaration.Variable(
                    Identifier("x") to Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }
    }
})
