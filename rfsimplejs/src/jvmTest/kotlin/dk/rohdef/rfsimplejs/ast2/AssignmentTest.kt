package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.Assignment
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Assignments")
class AssignmentTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("simple number assignment") {
        val code = "a = 1"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Assignment(
                Identifier("a"),
                Number.SimpleNumber.IntegerNumber(1),
            ),
        )
    }

    test("simple null assignment") {
        val code = "item = null"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Assignment(
                Identifier("item"),
                Null,
            ),
        )
    }

    test("assign identifier to identifier") {
        val code = "a = b"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Assignment(
                Identifier("a"),
                Identifier("b"),
            ),
        )
    }

    test("assign expression result") {
        val code = "x = 1 + 2"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Assignment(
                Identifier("x"),
                ArithmeticExpression.Addition(
                    Number.SimpleNumber.IntegerNumber(1),
                    Number.SimpleNumber.IntegerNumber(2),
                ),
            ),
        )
    }

    test("chained assignments") {
        val code = "a = b = 3"

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Assignment(
                Identifier("a"),
                Assignment(
                    Identifier("b"),
                    Number.SimpleNumber.IntegerNumber(3),
                ),
            ),
        )
    }

    test("assignment with trailing semicolon and whitespace") {
        val code = "  a = 42;  "

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Assignment(
                Identifier("a"),
                Number.SimpleNumber.IntegerNumber(42),
            ),
        )
    }
})
