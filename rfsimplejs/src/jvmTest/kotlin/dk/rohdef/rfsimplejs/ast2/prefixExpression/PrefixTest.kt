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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PrefixTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("Not (!)") {
        test("should negate boolean") {
            val code = "!true"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    Boolean.True,
                )
            )
        }

        test("should negate number") {
            val code = "!1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    Number.SimpleNumber.IntegerNumber(1),
                )
            )
        }

        test("should negate string") {
            val code = "!\"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    StringLiteral("false"),
                )
            )
        }

        test("should negate identifier") {
            val code = "!foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    Identifier("foo"),
                )
            )
        }

        test("should negate array and object") {
            val code = "![]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Not(
                    ArrayLiteral(),
                )
            )
        }

        test("should negate with odd whitespaces") {
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

    context("Plus (+)") {
        test("should unary plus boolean") {
            val code = "+true;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Plus(
                    Boolean.True,
                )
            )
        }

        test("should unary plus string") {
            val code = "+\"42\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Plus(
                    StringLiteral("42"),
                )
            )
        }

        test("should unary plus identifier") {
            val code = "+foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Plus(
                    Identifier("foo"),
                )
            )
        }

        test("should unary plus with odd whitespaces") {
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

    context("Minus (-)") {
        test("should unary minus boolean") {
            val code = "-true;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    Boolean.True,
                )
            )
        }

        test("should unary minus string") {
            val code = "-\"42\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    StringLiteral("42"),
                )
            )
        }

        test("should unary minus identifier") {
            val code = "-foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    Identifier("foo"),
                )
            )
        }

        test("should unary minus with odd whitespaces") {
            val code = "-   \t \n29.5;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Minus(
                    Number.SimpleNumber.DecimalNumber("29.5".toBigDecimal()),
                )
            )
        }
    }

    context("Increment (++)") {
        test("should increment identifier") {
            val code = "++foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    Identifier("foo"),
                )
            )
        }

        test("should increment number") {
            val code = "++1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    Number.SimpleNumber.IntegerNumber(1),
                )
            )
        }

        test("should increment chained call") {
            val code = "++foo.bar;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    )
                )
            )
        }

        test("should increment array reference") {
            val code = "++foo[0]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    )
                )
            )
        }

        test("should increment with odd whitespaces") {
            val code = "++   \t \ncount;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Increment(
                    Identifier("count"),
                )
            )
        }
    }

    context("Decrement (--)") {
        test("should decrement identifier") {
            val code = "--foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    Identifier("foo"),
                )
            )
        }

        test("should decrement number") {
            val code = "--1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    Number.SimpleNumber.IntegerNumber(1),
                )
            )
        }

        test("should decrement chained call") {
            val code = "--foo.bar;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    )
                )
            )
        }

        test("should decrement array reference") {
            val code = "--foo[0]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    )
                )
            )
        }

        test("should decrement with odd whitespaces") {
            val code = "--   \t \ncount;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Decrement(
                    Identifier("count"),
                )
            )
        }
    }

    context("TypeOf (typeof)") {
        test("should typeof number") {
            val code = "typeof 1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Number.SimpleNumber.IntegerNumber(1),
                )
            )
        }

        test("should typeof boolean") {
            val code = "typeof true;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Boolean.True,
                )
            )
        }

        test("should typeof string") {
            val code = "typeof \"hello\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    StringLiteral("hello"),
                )
            )
        }

        test("should typeof identifier") {
            val code = "typeof foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Identifier("foo"),
                )
            )
        }

        test("should typeof object") {
            val code = "typeof {}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    ObjectLiteral(),
                )
            )
        }

        test("should typeof with odd whitespaces") {
            val code = "typeof   \t \nfoo;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.TypeOf(
                    Identifier("foo"),
                )
            )
        }
    }

    context("Delete (delete)") {
        test("should delete identifier") {
            val code = "delete foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    Identifier("foo"),
                )
            )
        }

        test("should delete chained call") {
            val code = "delete foo.bar;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    ChainedCall(
                        Identifier("foo"),
                        Identifier("bar"),
                    )
                )
            )
        }

        test("should delete array reference") {
            val code = "delete foo[0]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    ArrayReference(
                        Identifier("foo"),
                        Number.SimpleNumber.IntegerNumber(0),
                    )
                )
            )
        }

        test("should delete string literal") {
            val code = "delete \"test\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    StringLiteral("test"),
                )
            )
        }

        test("should delete with odd whitespaces") {
            val code = "delete   \t \nfoo;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Delete(
                    Identifier("foo"),
                )
            )
        }
    }

    context("New (new)") {
        test("should new function call without arguments") {
            val code = "new Foo()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    FunctionCall(
                        Identifier("Foo"),
                    )
                )
            )
        }

        test("should new function call with arguments") {
            val code = "new Foo(\"bar\", 1);"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    FunctionCall(
                        Identifier("Foo"),
                        StringLiteral("bar"),
                        Number.SimpleNumber.IntegerNumber(1),
                    )
                )
            )
        }

        test("should new identifier") {
            val code = "new Foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    Identifier("Foo"),
                )
            )
        }

        test("should new chained call") {
            val code = "new foo.Bar()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    ChainedCall(
                        Identifier("foo"),
                        FunctionCall(
                            Identifier("Bar"),
                        )
                    )
                )
            )
        }

        test("should new with odd whitespaces") {
            val code = "new   \t \nFoo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.New(
                    FunctionCall(
                        Identifier("Foo"),
                    )
                )
            )
        }
    }

    context("Void (void)") {
        test("should void number") {
            val code = "void 0"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Number.SimpleNumber.IntegerNumber(0),
                )
            )
        }

        test("should void function call") {
            val code = "void foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    FunctionCall(
                        Identifier("foo"),
                    )
                )
            )
        }

        test("should void identifier") {
            val code = "void foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Identifier("foo"),
                )
            )
        }

        test("should void boolean") {
            val code = "void true"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Boolean.True,
                )
            )
        }

        test("should void with odd whitespaces") {
            val code = "void   \t \n0;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Void(
                    Number.SimpleNumber.IntegerNumber(0),
                )
            )
        }
    }

    context("Await (await)") {
        test("should await identifier") {
            val code = "await foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    Identifier("foo"),
                )
            )
        }

        test("should await function call") {
            val code = "await foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    FunctionCall(
                        Identifier("foo"),
                    )
                )
            )
        }

        test("should await chained call") {
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

        test("should await string literal") {
            val code = "await \"result\""

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    StringLiteral("result"),
                )
            )
        }

        test("should await with odd whitespaces") {
            val code = "await   \t \nfetch();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.Await(
                    FunctionCall(
                        Identifier("fetch"),
                    )
                )
            )
        }
    }
})
