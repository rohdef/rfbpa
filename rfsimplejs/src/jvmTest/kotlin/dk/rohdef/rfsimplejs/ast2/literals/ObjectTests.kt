package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ObjectTests : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("empty") {
        val code = "{}"

        val result = parser.run(code)

        result.stackTop shouldBe Program(
            ObjectLiteral()
        )
    }

    test("1") {
        val code = """
            { day: "Tag", bar: 19 }
        """.trimIndent()

        val result = parser.run(code)

        result.stackTop shouldBe Program(
            ObjectLiteral(
                Identifier("day") to StringLiteral("Tag"),
                Identifier("bar") to Number.SimpleNumber.IntegerNumber(19),
            )
        )
    }

    test("2") {
        val code = """
            { 
                day: "Tag",
                bar: 19,
            }
        """.trimIndent()

        val result = parser.run(code)

        result.stackTop shouldBe Program(
            ObjectLiteral(
                Identifier("day") to StringLiteral("Tag"),
                Identifier("bar") to Number.SimpleNumber.IntegerNumber(19),
            )
        )
    }
})