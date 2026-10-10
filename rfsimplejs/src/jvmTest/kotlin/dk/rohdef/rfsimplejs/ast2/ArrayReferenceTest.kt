package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.ArrayReference
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.PrefixExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number as AstNumber

class ArrayReferenceTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("Simple array reference") {
        val code = "arrayReference[0]"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ArrayReference(
                Identifier("arrayReference"),
                AstNumber.SimpleNumber.IntegerNumber(0),
            ),
        )
    }

    test("Array reference with identifier") {
        val code = "ada[identifier]"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ArrayReference(
                Identifier("ada"),
                Identifier("identifier"),
            ),
        )
    }

    test("Array reference on result and on result") {
        val code = "rising[foo][3][\"hey\"]"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ArrayReference(
                ArrayReference(
                    ArrayReference(
                        Identifier("rising"),
                        Identifier("foo"),
                    ),
                    AstNumber.SimpleNumber.IntegerNumber(3),
                ),
                StringLiteral("hey"),
            ),
        )
    }

    test("Array reference with funky parameters") {
        val code = "turing[anotherFuction(2, 3 - true)][5 + 3][[ true, true, false ]]"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ArrayReference(
                ArrayReference(
                    ArrayReference(
                        Identifier("turing"),
                        FunctionCall(
                            Identifier("anotherFuction"),
                            AstNumber.SimpleNumber.IntegerNumber(2),
                            ArithmeticExpression.Subtraction(
                                AstNumber.SimpleNumber.IntegerNumber(3),
                                AstBoolean.True,
                            ),
                        ),
                    ),
                    ArithmeticExpression.Addition(
                        AstNumber.SimpleNumber.IntegerNumber(5),
                        AstNumber.SimpleNumber.IntegerNumber(3),
                    ),
                ),
                ArrayLiteral(
                    AstBoolean.True,
                    AstBoolean.True,
                    AstBoolean.False,
                ),
            ),
        )
    }

    test("Funky array references") {
        val code = "+d[1]-1"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            ArithmeticExpression.Subtraction(
                PrefixExpression.Plus(
                    ArrayReference(
                        Identifier("d"),
                        AstNumber.SimpleNumber.IntegerNumber(1),
                    ),
                ),
                AstNumber.SimpleNumber.IntegerNumber(1),
            )
        )
    }
})
