package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
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

@DisplayName("Function calls")
class FunctionCallTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("simple") {
        val code = "functionCall()"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            FunctionCall(
                Identifier("functionCall")
            )
        )
    }

    test("with parameters") {
        val code = "ada(\"1\", 2, true, identifier)"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            FunctionCall(
                Identifier("ada"),
                StringLiteral("1"),
                AstNumber.SimpleNumber.IntegerNumber(2),
                AstBoolean.True,
                Identifier("identifier"),
            )
        )
    }

    test("continuously on the result") {
        val code = "rising(foo)(3)()(\"hey\")"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            FunctionCall(
                FunctionCall(
                    FunctionCall(
                        FunctionCall(
                            Identifier("rising"),
                            Identifier("foo"),
                        ),
                        AstNumber.SimpleNumber.IntegerNumber(3)
                    ),
                ),
                StringLiteral("hey"),
            )
        )
    }

    test("with funky parameters") {
        // TODO add array reference once supported
        val code = "turing(anotherFuction(2, 3 - true), 5 + 3, [ true, true, false ])"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            FunctionCall(
                Identifier("turing"),
                FunctionCall(
                    Identifier("anotherFuction"),
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
        )
    }
})