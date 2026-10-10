package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.ChainedCall
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number as AstNumber

@DisplayName("Chained call")
class ChainedCallTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("simple property access") {
        val code = "foo.bar"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                Identifier("foo"),
                Identifier("bar"),
            ),
        )
    }

    test("on a function call and property") {
        val code = "foo().bar"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                FunctionCall(
                    Identifier("foo"),
                ),
                Identifier("bar"),
            ),
        )
    }

    test("on a property and function call") {
        val code = "foo.bar()"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                Identifier("foo"),
                FunctionCall(
                    Identifier("bar"),
                ),
            ),
        )
    }

    test("on a function call and function call") {
        val code = "foo().bar()"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                FunctionCall(
                    Identifier("foo"),
                ),
                FunctionCall(
                    Identifier("bar"),
                ),
            ),
        )
    }

    test("takes precedence") {
        val code = "foo['hey'].bar() + baz().boo[4]"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ArithmeticExpression.Addition(
                ChainedCall(
                    ArrayReference(
                        Identifier("foo"),
                        StringLiteral("hey"),
                    ),
                    FunctionCall(
                        Identifier("bar"),
                    ),
                ),
                ChainedCall(
                    FunctionCall(
                        Identifier("baz"),
                    ),
                    ArrayReference(
                        Identifier("boo"),
                        AstNumber.SimpleNumber.IntegerNumber(4),
                    ),
                ),
            )
        )
    }

    test("continuously") {
        val code = "a.b.c.d"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
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
        )
    }

    test("on a function call with parameters") {
        val code = "ada.compute('1', 2, true, identifier).result"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                Identifier("ada"),
                ChainedCall(
                    FunctionCall(
                        Identifier("compute"),
                        StringLiteral("1"),
                        AstNumber.SimpleNumber.IntegerNumber(2),
                        AstBoolean.True,
                        Identifier("identifier"),
                    ),
                    Identifier("result"),
                ),
            ),
        )
    }

    test("on an array reference") {
        val code = "items[0].name"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                ArrayReference(
                    Identifier("items"),
                    AstNumber.SimpleNumber.IntegerNumber(0),
                ),
                Identifier("name"),
            ),
        )
    }

    test("on an array reference in property") {
        val code = "user.roles[1].permissions"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                Identifier("user"),
                ChainedCall(
                    ArrayReference(
                        Identifier("roles"),
                        AstNumber.SimpleNumber.IntegerNumber(1),
                    ),
                    Identifier("permissions"),
                ),
            ),
        )
    }

    test("mixing function call and array reference") {
        val code = "getData().items[2].getValue()"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                FunctionCall(
                    Identifier("getData"),
                ),
                ChainedCall(
                    ArrayReference(
                        Identifier("items"),
                        AstNumber.SimpleNumber.IntegerNumber(2),
                    ),
                    FunctionCall(
                        Identifier("getValue"),
                    ),
                ),
            ),
        )
    }

    test("with funky parameters") {
        val code = "turing.calculate(anotherFunction(2, 3 - true), 5 + 3, [ true, true, false ]).output"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ChainedCall(
                Identifier("turing"),
                ChainedCall(
                    FunctionCall(
                        Identifier("calculate"),
                        FunctionCall(
                            Identifier("anotherFunction"),
                            AstNumber.SimpleNumber.IntegerNumber(2),
                            ArithmeticExpression.Subtraction(
                                AstNumber.SimpleNumber.IntegerNumber(3),
                                AstBoolean.True,
                            ),
                        ),
                        ArithmeticExpression.Addition(
                            AstNumber.SimpleNumber.IntegerNumber(5),
                            AstNumber.SimpleNumber.IntegerNumber(3),
                        ),
                        ArrayLiteral(
                            AstBoolean.True,
                            AstBoolean.True,
                            AstBoolean.False,
                        ),
                    ),
                    Identifier("output"),
                ),
            ),
        )
    }
})