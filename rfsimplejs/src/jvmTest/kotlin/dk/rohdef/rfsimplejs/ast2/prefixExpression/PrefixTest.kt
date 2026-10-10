package dk.rohdef.rfsimplejs.ast2.prefixExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.ChainedCall
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Prefix expression")
class PrefixTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("not (!)") {
        test("negates boolean") {
            val code = "!true"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    Boolean.True,
                )
            )
        }

        test("negates numbers") {
            val code = "!1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    Number.SimpleNumber.IntegerNumber(1),
                )
            )
        }

        test("negates string") {
            val code = "!\"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    StringLiteral("false"),
                )
            )
        }

        test("negates identifier") {
            val code = "!foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    Identifier("foo"),
                )
            )
        }

        test("negates array and object") {
            val code = "![]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    ArrayLiteral(),
                )
            )
        }

        test("negates with odd whitespaces") {
            val code = "!   \t \n42;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    Number.SimpleNumber.IntegerNumber(42),
                )
            )
        }
    }

    context("plus (+)") {
        test("unary pluses boolean") {
            val code = "+true;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Plus(
                    Boolean.True,
                )
            )
        }

        test("unary pluses string") {
            val code = "+\"42\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Plus(
                    StringLiteral("42"),
                )
            )
        }

        test("unary pluses identifier") {
            val code = "+foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Plus(
                    Identifier("foo"),
                )
            )
        }

        test("unary pluses with odd whitespaces") {
            val code = "+   \t \n29.5;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Plus(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                )
            )
        }
    }

    context("minus (-)") {
        test("unary minuses boolean") {
            val code = "-true;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    Boolean.True,
                ),
            )
        }

        test("unary minuses string") {
            val code = "-\"42\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    StringLiteral("42"),
                ),
            )
        }

        test("unary minuses identifier") {
            val code = "-foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    Identifier("foo"),
                ),
            )
        }

        test("unary minuses with odd whitespaces") {
            val code = "-   \t \n29.5;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                ),
            )
        }
    }

    context("increment (++)") {
        test("identifier") {
            val code = "++foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    Identifier("foo"),
                ),
            )
        }

        test("number") {
            val code = "++1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("chained call") {
            val code = "++foo.bar;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    ),
                ),
            )
        }

        test("array reference") {
            val code = "++foo[0]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "++   \t \ncount;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    Identifier("count"),
                ),
            )
        }
    }

    context("decrement (--)") {
        test("identifier") {
            val code = "--foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    Identifier("foo"),
                ),
            )
        }

        test("number") {
            val code = "--1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("chained call") {
            val code = "--foo.bar;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    ),
                ),
            )
        }

        test("array reference") {
            val code = "--foo[0]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "--   \t \ncount;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    Identifier("count"),
                ),
            )
        }
    }

    context("typeof (typeof)") {
        test("number") {
            val code = "typeof 1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("boolean") {
            val code = "typeof true;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Boolean.True,
                ),
            )
        }

        test("string") {
            val code = "typeof \"hello\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    StringLiteral("hello"),
                ),
            )
        }

        test("identifier") {
            val code = "typeof foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Identifier("foo"),
                ),
            )
        }

        test("object") {
            val code = "typeof {}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    ObjectLiteral(),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "typeof   \t \nfoo;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Identifier("foo"),
                ),
            )
        }
    }

    context("delete (delete)") {
        test("identifier") {
            val code = "delete foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    Identifier("foo"),
                ),
            )
        }

        test("chained call") {
            val code = "delete foo.bar;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    ),
                ),
            )
        }

        test("array reference") {
            val code = "delete foo[0]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    ),
                ),
            )
        }

        test("string literal") {
            val code = "delete \"test\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    StringLiteral("test"),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "delete   \t \nfoo;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    Identifier("foo"),
                ),
            )
        }
    }

    context("new (new)") {
        test("function call without arguments") {
            val code = "new Foo()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    FunctionCall(
                        Identifier("Foo"),
                    ),
                ),
            )
        }

        test("function call with arguments") {
            val code = "new Foo(\"bar\", 1);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    FunctionCall(
                        Identifier("Foo"),
                        StringLiteral("bar"),
                        Number.SimpleNumber.IntegerNumber(1),
                    ),
                ),
            )
        }

        test("identifier") {
            val code = "new Foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    Identifier("Foo"),
                ),
            )
        }

        test("chained call") {
            val code = "new foo.Bar()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    ChainedCall(
                        Identifier("foo"),
                        FunctionCall(
                            Identifier("Bar"),
                        ),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "new   \t \nFoo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    FunctionCall(
                        Identifier("Foo"),
                    ),
                ),
            )
        }
    }

    context("void (void)") {
        test("number") {
            val code = "void 0"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Number.SimpleNumber.IntegerNumber(0),
                ),
            )
        }

        test("function call") {
            val code = "void foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    FunctionCall(
                        Identifier("foo"),
                    ),
                )
            )
        }

        test("identifier") {
            val code = "void foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Identifier("foo"),
                ),
            )
        }

        test("boolean") {
            val code = "void true"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Boolean.True,
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "void   \t \n0;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Number.SimpleNumber.IntegerNumber(0),
                ),
            )
        }
    }

    context("await (await)") {
        test("identifier") {
            val code = "await foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    Identifier("foo"),
                ),
            )
        }

        test("function call") {
            val code = "await foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    FunctionCall(
                        Identifier("foo"),
                    ),
                ),
            )
        }

        test("chained call") {
            val code = "await foo.bar()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    ChainedCall(
                        Identifier("foo"),
                        FunctionCall(
                            Identifier("bar"),
                        ),
                    ),
                ),
            )
        }

        test("string literal") {
            val code = "await \"result\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    StringLiteral("result"),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "await   \t \nfetch();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    FunctionCall(
                        Identifier("fetch"),
                    ),
                ),
            )
        }
    }
})
