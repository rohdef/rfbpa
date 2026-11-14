package dk.rohdef.rfsimplejs.ast2.binaryExpression

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.Declaration
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.ChainedCall
import dk.rohdef.rfsimplejs.ast2.statement.EnclosedExpression
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.BitwiseExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.*
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Bitwise expressions")
class BitwiseTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("And `&`") {
        test("of two numbers") {
            val code = "5 & 3"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    Number.SimpleNumber.IntegerNumber(5),
                    Number.SimpleNumber.IntegerNumber(3),
                ),
            )
        }

        test("of identifiers") {
            val code = "a & b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    Identifier("a"),
                    Identifier("b"),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true & \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]&{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3 & -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29   \t&   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    Number.SimpleNumber.IntegerNumber(29),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "2 & 3 & 4;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    BitwiseExpression.And(
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                    Number.SimpleNumber.IntegerNumber(4),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() & baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
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

    context("Or `|`") {
        test("of two numbers") {
            val code = "5 | 3"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
                    Number.SimpleNumber.IntegerNumber(5),
                    Number.SimpleNumber.IntegerNumber(3),
                ),
            )
        }

        test("of identifiers") {
            val code = "a | b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
                    Identifier("a"),
                    Identifier("b"),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true | \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]|{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3 | -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29   \t|   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
                    Number.SimpleNumber.IntegerNumber(29),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "2 | 3 | 4;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
                    BitwiseExpression.Or(
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                    Number.SimpleNumber.IntegerNumber(4),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() | baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Or(
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

    context("Xor `^`") {
        test("of two numbers") {
            val code = "5 ^ 3"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
                    Number.SimpleNumber.IntegerNumber(5),
                    Number.SimpleNumber.IntegerNumber(3),
                ),
            )
        }

        test("of identifiers") {
            val code = "a ^ b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
                    Identifier("a"),
                    Identifier("b"),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true ^ \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]^{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3 ^ -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29   \t^   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
                    Number.SimpleNumber.IntegerNumber(29),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "2 ^ 3 ^ 4;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
                    BitwiseExpression.Xor(
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                    Number.SimpleNumber.IntegerNumber(4),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() ^ baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.Xor(
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

    context("Left shift `<<`") {
        test("of two numbers") {
            val code = "5 << 3"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
                    Number.SimpleNumber.IntegerNumber(5),
                    Number.SimpleNumber.IntegerNumber(3),
                ),
            )
        }

        test("of identifiers") {
            val code = "a << b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
                    Identifier("a"),
                    Identifier("b"),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true << \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]<<{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3 << -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29   \t<<   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
                    Number.SimpleNumber.IntegerNumber(29),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "2 << 3 << 4;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
                    BitwiseExpression.LeftShift(
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                    Number.SimpleNumber.IntegerNumber(4),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() << baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.LeftShift(
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

    context("Sign-propagating right shift `>>`") {
        test("of two numbers") {
            val code = "8 >> 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
                    Number.SimpleNumber.IntegerNumber(8),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("of identifiers") {
            val code = "a >> b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
                    Identifier("a"),
                    Identifier("b"),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true >> \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]>>{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3 >> -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29   \t>>   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
                    Number.SimpleNumber.IntegerNumber(29),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "8 >> 2 >> 1;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
                    BitwiseExpression.RightShift(
                        Number.SimpleNumber.IntegerNumber(8),
                        Number.SimpleNumber.IntegerNumber(2),
                    ),
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() >> baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.RightShift(
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

    context("Zero-fill right shift `>>>`") {
        test("of two numbers") {
            val code = "8 >>> 2"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
                    Number.SimpleNumber.IntegerNumber(8),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            )
        }

        test("of identifiers") {
            val code = "a >>> b"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
                    Identifier("a"),
                    Identifier("b"),
                ),
            )
        }

        test("of boolean and string") {
            val code = "true >>> \"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
                    Boolean.True,
                    StringLiteral("false"),
                ),
            )
        }

        test("of array and object") {
            val code = "[]>>>{}"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
                    ArrayLiteral(),
                    ObjectLiteral(),
                ),
            )
        }

        test("with negative number") {
            val code = "3 >>> -9"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
                    Number.SimpleNumber.IntegerNumber(3),
                    PrefixExpression.Minus(
                        Number.SimpleNumber.IntegerNumber(9),
                    ),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "29   \t>>>   \n19;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
                    Number.SimpleNumber.IntegerNumber(29),
                    Number.SimpleNumber.IntegerNumber(19),
                ),
            )
        }

        test("associates to the left") {
            val code = "8 >>> 2 >>> 1;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
                    BitwiseExpression.UnsignedRightShift(
                        Number.SimpleNumber.IntegerNumber(8),
                        Number.SimpleNumber.IntegerNumber(2),
                    ),
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("chained calls get nested") {
            val code = """
                foo.bar() >>> baz[0].qux
            """.trimIndent()

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.UnsignedRightShift(
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

    context("Bitwise not `~`") {
        test("of number") {
            val code = "~1"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("of boolean") {
            val code = "~true"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    Boolean.True,
                ),
            )
        }

        test("of string") {
            val code = "~\"false\";"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    StringLiteral("false"),
                ),
            )
        }

        test("of identifier") {
            val code = "~foo"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    Identifier("foo"),
                ),
            )
        }

        test("of array and object") {
            val code = "~[]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    ArrayLiteral(),
                ),
            )
        }

        test("with odd whitespaces") {
            val code = "~   \t \n42;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    Number.SimpleNumber.IntegerNumber(42),
                ),
            )
        }

        test("of function call") {
            val code = "~foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    FunctionCall(
                        Identifier("foo"),
                    ),
                ),
            )
        }

        test("of chained call") {
            val code = "~foo.bar()"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    ChainedCall(
                        Identifier("foo"),
                        FunctionCall(
                            Identifier("bar"),
                        ),
                    ),
                ),
            )
        }

        test("double bitwise not") {
            val code = "~~42"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                PrefixExpression.BitwiseNot(
                    PrefixExpression.BitwiseNot(
                        Number.SimpleNumber.IntegerNumber(42),
                    ),
                ),
            )
        }
    }

    context("declarations and statements with bitwise expressions") {
        test("const declaration with bitwise and") {
            val code = "const mask = a & 0xFF;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Constant(
                    Identifier("mask") to BitwiseExpression.And(
                        Identifier("a"),
                        Number.HexadecimalNumber("0xFF"),
                    ),
                ),
            )
        }

        test("let declaration with bitwise shift") {
            val code = "let shifted = value << 2;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Let(
                    Identifier("shifted") to BitwiseExpression.LeftShift(
                        Identifier("value"),
                        Number.SimpleNumber.IntegerNumber(2),
                    ),
                ),
            )
        }

        test("enclosed bitwise expressions") {
            val code = "(a | b) & (c ^ d)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                BitwiseExpression.And(
                    EnclosedExpression(
                        BitwiseExpression.Or(
                            Identifier("a"),
                            Identifier("b"),
                        ),
                    ),
                    EnclosedExpression(
                        BitwiseExpression.Xor(
                            Identifier("c"),
                            Identifier("d"),
                        ),
                    ),
                ),
            )
        }
    }

    context("toCode") {
        test("And toCode") {
            val expr = BitwiseExpression.And(Identifier("a"), Identifier("b"))
            expr.toCode() shouldBe "a & b"
        }

        test("Or toCode") {
            val expr = BitwiseExpression.Or(Identifier("a"), Identifier("b"))
            expr.toCode() shouldBe "a | b"
        }

        test("Xor toCode") {
            val expr = BitwiseExpression.Xor(Identifier("a"), Identifier("b"))
            expr.toCode() shouldBe "a ^ b"
        }

        test("LeftShift toCode") {
            val expr = BitwiseExpression.LeftShift(Identifier("a"), Identifier("b"))
            expr.toCode() shouldBe "a << b"
        }

        test("RightShift toCode") {
            val expr = BitwiseExpression.RightShift(Identifier("a"), Identifier("b"))
            expr.toCode() shouldBe "a >> b"
        }

        test("UnsignedRightShift toCode") {
            val expr = BitwiseExpression.UnsignedRightShift(Identifier("a"), Identifier("b"))
            expr.toCode() shouldBe "a >>> b"
        }

        test("BitwiseNot toCode") {
            val expr = PrefixExpression.BitwiseNot(Identifier("a"))
            expr.toCode() shouldBe "~a"
        }
    }
})
